package com.smarthealthcare.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.dto.LoginRequest;
import com.smarthealthcare.dto.LoginResponse;
import com.smarthealthcare.dto.RegisterRequest;
import com.smarthealthcare.entity.SysRole;
import com.smarthealthcare.entity.SysUser;
import com.smarthealthcare.mapper.SysRoleMapper;
import com.smarthealthcare.mapper.SysUserMapper;
import com.smarthealthcare.security.JwtUtils;
import com.smarthealthcare.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 获取角色信息
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "ROLE_PATIENT";
        String roleName = role != null ? role.getRoleName() : "患者";

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roleCode);

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // Token存入Redis（用于登出控制）
        redisTemplate.opsForValue().set("token:" + user.getId(), token, 24, TimeUnit.HOURS);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(roleCode)
                .roleName(roleName)
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 校验用户名唯一性
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setRoleId(1L); // 默认患者角色
        user.setStatus(1);

        sysUserMapper.insert(user);
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete("token:" + userId);
    }
}

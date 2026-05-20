package com.smarthealthcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthealthcare.common.Result;
import com.smarthealthcare.entity.SysLog;
import com.smarthealthcare.entity.SysRole;
import com.smarthealthcare.entity.SysUser;
import com.smarthealthcare.mapper.SysLogMapper;
import com.smarthealthcare.mapper.SysRoleMapper;
import com.smarthealthcare.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统管理接口", description = "用户管理、角色管理、日志监控")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysLogMapper logMapper;
    private final PasswordEncoder passwordEncoder;

    // ========== 用户管理 ==========

    @Operation(summary = "用户列表（分页）")
    @GetMapping("/user/page")
    public Result<Page<SysUser>> userPage(@RequestParam(defaultValue = "1") int current,
                                           @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> page = userMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreateTime));
        return Result.success(page);
    }

    @Operation(summary = "创建用户")
    @PostMapping("/user")
    public Result<Void> createUser(@RequestBody SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return Result.success("用户创建成功");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/user")
    public Result<Void> updateUser(@RequestBody SysUser user) {
        // 密码不在此处更新（单独提供重置密码接口）
        user.setPassword(null);
        userMapper.updateById(user);
        return Result.success("用户更新成功");
    }

    @Operation(summary = "重置密码")
    @PutMapping("/user/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
        }
        return Result.success("密码重置成功");
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/user/{id}/toggle-status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    // ========== 角色管理 ==========

    @Operation(summary = "角色列表")
    @GetMapping("/role")
    public Result<List<SysRole>> listRoles() {
        return Result.success(roleMapper.selectList(null));
    }

    // ========== 日志监控 ==========

    @Operation(summary = "系统日志（分页）")
    @GetMapping("/log/page")
    public Result<Page<SysLog>> logPage(@RequestParam(defaultValue = "1") int current,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<SysLog> page = logMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysLog>().orderByDesc(SysLog::getCreateTime));
        return Result.success(page);
    }
}

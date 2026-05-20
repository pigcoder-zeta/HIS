package com.smarthealthcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthealthcare.entity.SysUser;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT u.*, r.role_code, d.dept_name FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "LEFT JOIN sys_department d ON u.dept_id = d.id " +
            "WHERE u.id = #{id}")
    SysUser selectUserWithDetails(Long id);

    @Select("SELECT u.*, r.role_code FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.username = #{username}")
    SysUser selectByUsername(String username);
}

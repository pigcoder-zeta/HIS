package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Long roleId;
    private Long deptId;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private Integer status;
    private String title;
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段 - 角色编码 */
    @TableField(exist = false)
    private String roleCode;

    /** 非数据库字段 - 科室名称 */
    @TableField(exist = false)
    private String deptName;
}

package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_department")
public class SysDepartment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String deptName;
    private String deptCode;
    private Long parentId;
    private String description;
    private String address;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_schedule")
public class BizSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private Long deptId;
    private LocalDate scheduleDate;
    private Integer timeSlot;       // 1-上午,2-下午,3-夜诊
    private Integer totalQuota;
    private Integer leftQuota;
    private BigDecimal fee;
    private Integer status;         // 0-停诊,1-正常

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String doctorName;

    @TableField(exist = false)
    private String deptName;
}

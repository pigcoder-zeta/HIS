package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_appointment")
public class BizAppointment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long scheduleId;
    private Long doctorId;
    private Long deptId;
    private LocalDate scheduleDate;
    private Integer timeSlot;
    private Integer queueNumber;
    private Integer status;         // 0-待支付,1-已预约,2-就诊中,3-已完成,4-已取消,5-已爽约
    private BigDecimal fee;
    private LocalDateTime cancelTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String patientName;

    @TableField(exist = false)
    private String doctorName;

    @TableField(exist = false)
    private String deptName;
}

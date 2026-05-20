package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_exam_appointment")
public class BizExamAppointment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long packageId;
    private LocalDate appointmentDate;
    private Integer status;
    private BigDecimal fee;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String patientName;

    @TableField(exist = false)
    private String packageName;
}

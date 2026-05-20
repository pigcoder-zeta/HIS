package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_prescription")
public class BizPrescription {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long patientId;
    private Long doctorId;
    private Integer status;         // 0-待审核,1-已审核,2-已发药,3-已驳回
    private Long pharmacistId;
    private String auditOpinion;
    private BigDecimal totalAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

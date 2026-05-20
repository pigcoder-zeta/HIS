package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_prescription_item")
public class BizPrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private String drugName;
    private String dosage;
    private String usageMethod;
    private Integer quantity;
    private BigDecimal unitPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

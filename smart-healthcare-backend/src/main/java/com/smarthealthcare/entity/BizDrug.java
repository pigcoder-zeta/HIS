package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_drug")
public class BizDrug {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String drugCode;
    private String drugName;
    private String genericName;
    private String category;
    private String specification;
    private String manufacturer;
    private String unit;
    private BigDecimal unitPrice;
    private Integer stockCount;
    private Integer safeThreshold;
    private LocalDate expiryDate;
    private Integer isHighRisk;
    private Integer status;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

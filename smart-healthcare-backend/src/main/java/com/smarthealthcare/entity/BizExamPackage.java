package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_exam_package")
public class BizExamPackage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String packageName;
    private String description;
    private BigDecimal price;
    private String suitableCrowd;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

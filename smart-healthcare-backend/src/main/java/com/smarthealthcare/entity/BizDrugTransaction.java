package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_drug_transaction")
public class BizDrugTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drugId;
    private Integer type;       // 1-入库,2-出库,3-发药扣减,4-退货入库
    private Integer quantity;
    private Integer beforeStock;
    private Integer afterStock;
    private String batchNo;
    private Long operatorId;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

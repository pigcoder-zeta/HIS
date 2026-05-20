package com.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_medical_record")
public class BizMedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private String chiefComplaint;          // 主诉(S)
    private String presentIllness;          // 现病史(S)
    private String physicalExamination;     // 查体结果(O)
    private String auxiliaryExam;           // 辅助检查(O)
    private String diagnosis;              // 初步诊断(A)
    private String treatmentPlan;           // 治疗方案(P)
    private Integer status;                 // 0-草稿,1-已签名,2-已归档,3-已锁定
    private LocalDateTime signedTime;
    private String signatureCode;
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String patientName;

    @TableField(exist = false)
    private String doctorName;
}

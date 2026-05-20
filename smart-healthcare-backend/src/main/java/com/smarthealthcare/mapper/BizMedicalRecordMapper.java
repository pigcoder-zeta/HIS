package com.smarthealthcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthealthcare.entity.BizMedicalRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BizMedicalRecordMapper extends BaseMapper<BizMedicalRecord> {

    @Select("SELECT m.*, p.real_name AS patient_name, d.real_name AS doctor_name " +
            "FROM biz_medical_record m " +
            "LEFT JOIN sys_user p ON m.patient_id = p.id " +
            "LEFT JOIN sys_user d ON m.doctor_id = d.id " +
            "WHERE m.patient_id = #{patientId} ORDER BY m.create_time DESC")
    List<BizMedicalRecord> selectByPatientId(@Param("patientId") Long patientId);
}

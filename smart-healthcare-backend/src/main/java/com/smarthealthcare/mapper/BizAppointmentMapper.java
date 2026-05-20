package com.smarthealthcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthealthcare.entity.BizAppointment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BizAppointmentMapper extends BaseMapper<BizAppointment> {

    @Select("SELECT a.*, p.real_name AS patient_name, d.real_name AS doctor_name, " +
            "dept.dept_name FROM biz_appointment a " +
            "LEFT JOIN sys_user p ON a.patient_id = p.id " +
            "LEFT JOIN sys_user d ON a.doctor_id = d.id " +
            "LEFT JOIN sys_department dept ON a.dept_id = dept.id " +
            "WHERE a.patient_id = #{patientId} ORDER BY a.create_time DESC")
    List<BizAppointment> selectByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT a.*, p.real_name AS patient_name, d.real_name AS doctor_name, " +
            "dept.dept_name FROM biz_appointment a " +
            "LEFT JOIN sys_user p ON a.patient_id = p.id " +
            "LEFT JOIN sys_user d ON a.doctor_id = d.id " +
            "LEFT JOIN sys_department dept ON a.dept_id = dept.id " +
            "WHERE a.doctor_id = #{doctorId} AND a.schedule_date = CURDATE() " +
            "AND a.status IN (1, 2) ORDER BY a.queue_number")
    List<BizAppointment> selectTodayByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT COUNT(*) FROM biz_appointment " +
            "WHERE patient_id = #{patientId} AND dept_id = #{deptId} " +
            "AND schedule_date = #{scheduleDate} AND status NOT IN (4, 5)")
    int countByPatientDeptDate(@Param("patientId") Long patientId,
                                @Param("deptId") Long deptId,
                                @Param("scheduleDate") String scheduleDate);
}

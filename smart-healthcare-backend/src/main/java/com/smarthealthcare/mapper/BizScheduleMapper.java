package com.smarthealthcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthealthcare.entity.BizSchedule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BizScheduleMapper extends BaseMapper<BizSchedule> {

    @Select("SELECT s.*, u.real_name AS doctor_name, d.dept_name FROM biz_schedule s " +
            "LEFT JOIN sys_user u ON s.doctor_id = u.id " +
            "LEFT JOIN sys_department d ON s.dept_id = d.id " +
            "WHERE s.dept_id = #{deptId} AND s.schedule_date >= #{startDate} AND s.schedule_date <= #{endDate} " +
            "AND s.status = 1 ORDER BY s.schedule_date, s.time_slot")
    List<BizSchedule> selectByDeptAndDateRange(@Param("deptId") Long deptId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 乐观锁扣减号源
     */
    @Update("UPDATE biz_schedule SET left_quota = left_quota - 1, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND left_quota > 0")
    int deductQuota(@Param("id") Long id, @Param("version") Integer version);
}

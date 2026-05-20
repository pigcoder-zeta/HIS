package com.smarthealthcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthealthcare.common.Result;
import com.smarthealthcare.entity.*;
import com.smarthealthcare.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "工作台接口", description = "Dashboard统计数据")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper userMapper;
    private final SysDepartmentMapper departmentMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final BizScheduleMapper scheduleMapper;
    private final BizPrescriptionMapper prescriptionMapper;
    private final BizDrugMapper drugMapper;
    private final SysLogMapper logMapper;

    // ==================== 管理员视图 ====================

    @Operation(summary = "管理员-总注册用户数趋势(近7天)")
    @GetMapping("/admin/user-trend")
    public Result<List<Map<String, Object>>> userTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .le(SysUser::getCreateTime, date.atTime(23, 59, 59)));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            item.put("count", count);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "管理员-今日各科室就诊量对比")
    @GetMapping("/admin/dept-visit-today")
    public Result<List<Map<String, Object>>> deptVisitToday() {
        List<SysDepartment> depts = departmentMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (SysDepartment dept : depts) {
            Long count = appointmentMapper.selectCount(new LambdaQueryWrapper<BizAppointment>()
                    .eq(BizAppointment::getDeptId, dept.getId())
                    .eq(BizAppointment::getScheduleDate, today)
                    .in(BizAppointment::getStatus, 1, 2, 3)); // 已预约、就诊中、已完成
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deptName", dept.getDeptName());
            item.put("count", count);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "管理员-系统概览卡片数据")
    @GetMapping("/admin/overview")
    public Result<Map<String, Object>> adminOverview() {
        Long totalUsers = userMapper.selectCount(null);
        Long totalDoctors = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleId, 2L));
        Long todayAppointments = appointmentMapper.selectCount(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getScheduleDate, LocalDate.now())
                .in(BizAppointment::getStatus, 1, 2, 3));
        Long totalDrugs = drugMapper.selectCount(new LambdaQueryWrapper<BizDrug>().eq(BizDrug::getStatus, 1));
        // 近24小时日志异常数
        Long errorLogs = logMapper.selectCount(new LambdaQueryWrapper<SysLog>()
                .ge(SysLog::getCreateTime, LocalDateTime.now().minusHours(24))
                .eq(SysLog::getStatus, 0));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalDoctors", totalDoctors);
        data.put("todayAppointments", todayAppointments);
        data.put("totalDrugs", totalDrugs);
        data.put("errorLogs", errorLogs);
        return Result.success(data);
    }

    @Operation(summary = "管理员-今日接口调用成功率")
    @GetMapping("/admin/api-success-rate")
    public Result<Map<String, Object>> apiSuccessRate() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Long total = logMapper.selectCount(new LambdaQueryWrapper<SysLog>()
                .ge(SysLog::getCreateTime, since));
        Long success = logMapper.selectCount(new LambdaQueryWrapper<SysLog>()
                .ge(SysLog::getCreateTime, since)
                .eq(SysLog::getStatus, 1));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("success", success);
        data.put("fail", total - success);
        data.put("rate", total > 0 ? Math.round(success * 10000.0 / total) / 100.0 : 100.0);
        return Result.success(data);
    }

    // ==================== 医生视图 ====================

    @Operation(summary = "医生-今日概览")
    @GetMapping("/doctor/overview")
    public Result<Map<String, Object>> doctorOverview(@RequestParam Long doctorId) {
        LocalDate today = LocalDate.now();
        // 待诊人数(status=1)
        Long waitingCount = appointmentMapper.selectCount(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getDoctorId, doctorId)
                .eq(BizAppointment::getScheduleDate, today)
                .eq(BizAppointment::getStatus, 1));
        // 已诊人数(status=2,3)
        Long treatedCount = appointmentMapper.selectCount(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getDoctorId, doctorId)
                .eq(BizAppointment::getScheduleDate, today)
                .in(BizAppointment::getStatus, 2, 3));
        // 今日处方数
        Long todayPrescriptions = prescriptionMapper.selectCount(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getDoctorId, doctorId)
                .ge(BizPrescription::getCreateTime, today.atStartOfDay()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("waitingCount", waitingCount);
        data.put("treatedCount", treatedCount);
        data.put("todayPrescriptions", todayPrescriptions);
        return Result.success(data);
    }

    @Operation(summary = "医生-本月排班日历")
    @GetMapping("/doctor/schedule-calendar")
    public Result<List<Map<String, Object>>> doctorScheduleCalendar(@RequestParam Long doctorId,
                                                                     @RequestParam String month) {
        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end = start.plusMonths(1).minusDays(1);
        List<BizSchedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getDoctorId, doctorId)
                .between(BizSchedule::getScheduleDate, start, end)
                .eq(BizSchedule::getStatus, 1));

        return Result.success(schedules.stream().map(s -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", s.getScheduleDate().toString());
            item.put("timeSlot", s.getTimeSlot());
            item.put("deptName", s.getDeptName());
            item.put("leftQuota", s.getLeftQuota());
            return item;
        }).collect(Collectors.toList()));
    }

    // ==================== 药房视图 ====================

    @Operation(summary = "药房-今日概览")
    @GetMapping("/pharmacy/overview")
    public Result<Map<String, Object>> pharmacyOverview() {
        LocalDate today = LocalDate.now();
        // 待审核处方数
        Long pendingCount = prescriptionMapper.selectCount(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getStatus, 0));
        // 今日发药数
        Long todayDispensed = prescriptionMapper.selectCount(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getStatus, 2)
                .ge(BizPrescription::getUpdateTime, today.atStartOfDay()));
        // 低库存药品数
        List<BizDrug> allDrugs = drugMapper.selectList(new LambdaQueryWrapper<BizDrug>().eq(BizDrug::getStatus, 1));
        long lowStockCount = allDrugs.stream().filter(d -> d.getStockCount() <= d.getSafeThreshold()).count();
        // 临期药品数(30天内)
        LocalDate deadline = today.plusDays(30);
        long nearExpiryCount = allDrugs.stream().filter(d -> d.getExpiryDate() != null && !d.getExpiryDate().isAfter(deadline)).count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pendingAuditCount", pendingCount);
        data.put("todayDispensedCount", todayDispensed);
        data.put("lowStockCount", lowStockCount);
        data.put("nearExpiryCount", nearExpiryCount);
        return Result.success(data);
    }

    @Operation(summary = "药房-低库存/临期预警滚动列表")
    @GetMapping("/pharmacy/alerts")
    public Result<List<Map<String, Object>>> pharmacyAlerts() {
        List<BizDrug> allDrugs = drugMapper.selectList(new LambdaQueryWrapper<BizDrug>().eq(BizDrug::getStatus, 1));
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(30);

        List<Map<String, Object>> alerts = new ArrayList<>();
        for (BizDrug drug : allDrugs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("drugName", drug.getDrugName());
            item.put("stockCount", drug.getStockCount());
            item.put("safeThreshold", drug.getSafeThreshold());
            item.put("expiryDate", drug.getExpiryDate() != null ? drug.getExpiryDate().toString() : null);

            if (drug.getStockCount() <= drug.getSafeThreshold()) {
                item.put("alertType", "低库存");
                item.put("alertLevel", drug.getStockCount() == 0 ? "danger" : "warning");
                alerts.add(item);
            } else if (drug.getExpiryDate() != null && !drug.getExpiryDate().isAfter(deadline)) {
                item.put("alertType", "临期");
                item.put("alertLevel", "warning");
                alerts.add(item);
            }
        }
        return Result.success(alerts);
    }

    // ==================== 医务科视图 ====================

    @Operation(summary = "医务科-概览")
    @GetMapping("/medical/overview")
    public Result<Map<String, Object>> medicalOverview() {
        LocalDate today = LocalDate.now();
        Long totalDepts = departmentMapper.selectCount(null);
        Long totalDoctors = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleId, 2L));
        Long todaySchedules = scheduleMapper.selectCount(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getScheduleDate, today)
                .eq(BizSchedule::getStatus, 1));
        Long todayAppointments = appointmentMapper.selectCount(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getScheduleDate, today)
                .in(BizAppointment::getStatus, 1, 2, 3));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalDepts", totalDepts);
        data.put("totalDoctors", totalDoctors);
        data.put("todaySchedules", todaySchedules);
        data.put("todayAppointments", todayAppointments);
        return Result.success(data);
    }
}

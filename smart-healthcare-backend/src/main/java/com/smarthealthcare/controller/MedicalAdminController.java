package com.smarthealthcare.controller;

import com.smarthealthcare.common.Result;
import com.smarthealthcare.dto.ScheduleDTO;
import com.smarthealthcare.entity.BizSchedule;
import com.smarthealthcare.entity.SysDepartment;
import com.smarthealthcare.entity.SysUser;
import com.smarthealthcare.mapper.SysDepartmentMapper;
import com.smarthealthcare.mapper.SysUserMapper;
import com.smarthealthcare.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "医务科接口", description = "排班管理、科室管理、报表")
@RestController
@RequestMapping("/api/v1/medical")
@RequiredArgsConstructor
public class MedicalAdminController {

    private final ScheduleService scheduleService;
    private final SysDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    // ========== 排班管理 ==========

    @Operation(summary = "查询科室周排班")
    @GetMapping("/schedule")
    public Result<List<BizSchedule>> listSchedules(
            @RequestParam Long deptId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(scheduleService.listByDeptAndWeek(deptId, startDate, endDate));
    }

    @Operation(summary = "创建单个排班")
    @PostMapping("/schedule")
    public Result<Void> createSchedule(@RequestBody ScheduleDTO dto) {
        scheduleService.createSchedule(dto);
        return Result.success("排班创建成功");
    }

    @Operation(summary = "智能批量排班")
    @PostMapping("/schedule/batch")
    public Result<Void> batchSchedule(@RequestBody List<ScheduleDTO> schedules) {
        scheduleService.batchCreate(schedules);
        return Result.success("批量排班完成");
    }

    @Operation(summary = "取消排班")
    @PutMapping("/schedule/{id}/cancel")
    public Result<Void> cancelSchedule(@PathVariable Long id) {
        scheduleService.cancelSchedule(id);
        return Result.success("排班已取消");
    }

    // ========== 科室管理 ==========

    @Operation(summary = "科室列表")
    @GetMapping("/department")
    public Result<List<SysDepartment>> listDepartments() {
        return Result.success(departmentMapper.selectList(null));
    }

    @Operation(summary = "新增科室")
    @PostMapping("/department")
    public Result<Void> addDepartment(@RequestBody SysDepartment department) {
        departmentMapper.insert(department);
        return Result.success("科室添加成功");
    }

    @Operation(summary = "更新科室")
    @PutMapping("/department")
    public Result<Void> updateDepartment(@RequestBody SysDepartment department) {
        departmentMapper.updateById(department);
        return Result.success("科室更新成功");
    }

    // ========== 医生管理 ==========

    @Operation(summary = "医生列表")
    @GetMapping("/doctor")
    public Result<List<SysUser>> listDoctors(@RequestParam(required = false) Long deptId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRoleId, 2L); // 角色2 = 医生
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        return Result.success(userMapper.selectList(wrapper));
    }
}

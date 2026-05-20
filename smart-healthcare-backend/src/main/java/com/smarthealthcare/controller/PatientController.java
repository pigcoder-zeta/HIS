package com.smarthealthcare.controller;

import com.smarthealthcare.common.Result;
import com.smarthealthcare.dto.AppointmentRequest;
import com.smarthealthcare.entity.BizAppointment;
import com.smarthealthcare.entity.BizExamAppointment;
import com.smarthealthcare.entity.BizExamPackage;
import com.smarthealthcare.entity.BizMedicalRecord;
import com.smarthealthcare.service.AppointmentService;
import com.smarthealthcare.service.ExamService;
import com.smarthealthcare.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "患者端接口", description = "挂号预约、病历查看、体检预约")
@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService recordService;
    private final ExamService examService;

    // ========== 挂号预约 ==========

    @Operation(summary = "预约挂号")
    @PostMapping("/appointment/book")
    public Result<BizAppointment> book(@Valid @RequestBody AppointmentRequest request) {
        return Result.success("预约成功", appointmentService.book(request.getPatientId(), request.getScheduleId()));
    }

    @Operation(summary = "取消预约")
    @PutMapping("/appointment/{id}/cancel")
    public Result<Void> cancelAppointment(@PathVariable Long id, @RequestParam Long patientId) {
        appointmentService.cancel(id, patientId);
        return Result.success("取消成功");
    }

    @Operation(summary = "我的挂号列表")
    @GetMapping("/appointment/list")
    public Result<List<BizAppointment>> listAppointments(@RequestParam Long patientId) {
        return Result.success(appointmentService.listByPatient(patientId));
    }

    // ========== 电子病历 ==========

    @Operation(summary = "我的病历列表")
    @GetMapping("/record/list")
    public Result<List<BizMedicalRecord>> listRecords(@RequestParam Long patientId) {
        return Result.success(recordService.listByPatient(patientId));
    }

    @Operation(summary = "查看病历详情")
    @GetMapping("/record/{id}")
    public Result<BizMedicalRecord> getRecord(@PathVariable Long id) {
        return Result.success(recordService.getById(id));
    }

    // ========== 体检预约 ==========

    @Operation(summary = "体检套餐列表")
    @GetMapping("/exam/packages")
    public Result<List<BizExamPackage>> listPackages() {
        return Result.success(examService.listPackages());
    }

    @Operation(summary = "预约体检")
    @PostMapping("/exam/book")
    public Result<BizExamAppointment> bookExam(@RequestParam Long patientId,
                                                @RequestParam Long packageId,
                                                @RequestParam String date) {
        return Result.success("体检预约成功", examService.book(patientId, packageId, date));
    }

    @Operation(summary = "我的体检预约")
    @GetMapping("/exam/list")
    public Result<List<BizExamAppointment>> listExams(@RequestParam Long patientId) {
        return Result.success(examService.listByPatient(patientId));
    }

    @Operation(summary = "取消体检预约")
    @PutMapping("/exam/{id}/cancel")
    public Result<Void> cancelExam(@PathVariable Long id, @RequestParam Long patientId) {
        examService.cancel(id, patientId);
        return Result.success("取消成功");
    }
}

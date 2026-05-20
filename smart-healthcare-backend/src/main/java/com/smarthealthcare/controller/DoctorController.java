package com.smarthealthcare.controller;

import com.smarthealthcare.common.Result;
import com.smarthealthcare.dto.MedicalRecordDTO;
import com.smarthealthcare.dto.PrescriptionDTO;
import com.smarthealthcare.entity.BizAppointment;
import com.smarthealthcare.entity.BizMedicalRecord;
import com.smarthealthcare.entity.BizPrescription;
import com.smarthealthcare.service.AppointmentService;
import com.smarthealthcare.service.MedicalRecordService;
import com.smarthealthcare.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "医生端接口", description = "接诊、病历书写、开处方")
@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService recordService;
    private final PrescriptionService prescriptionService;

    @Operation(summary = "今日待诊列表")
    @GetMapping("/appointment/today")
    public Result<List<BizAppointment>> todayAppointments(@RequestParam Long doctorId) {
        return Result.success(appointmentService.listTodayByDoctor(doctorId));
    }

    @Operation(summary = "呼叫下一位（更新就诊状态）")
    @PutMapping("/appointment/{id}/call")
    public Result<Void> callNext(@PathVariable Long id) {
        appointmentService.updateStatus(id, 2); // 就诊中
        return Result.success();
    }

    @Operation(summary = "完成就诊")
    @PutMapping("/appointment/{id}/complete")
    public Result<Void> completeAppointment(@PathVariable Long id) {
        appointmentService.updateStatus(id, 3); // 已完成
        return Result.success();
    }

    @Operation(summary = "创建/更新电子病历")
    @PostMapping("/record")
    public Result<BizMedicalRecord> saveRecord(@RequestParam Long doctorId,
                                                @Valid @RequestBody MedicalRecordDTO dto) {
        return Result.success(recordService.createOrUpdate(doctorId, dto));
    }

    @Operation(summary = "签名归档病历")
    @PutMapping("/record/{id}/sign")
    public Result<BizMedicalRecord> signRecord(@PathVariable Long id, @RequestParam Long doctorId) {
        return Result.success("病历已签名归档", recordService.signAndArchive(id, doctorId));
    }

    @Operation(summary = "开具电子处方")
    @PostMapping("/prescription")
    public Result<BizPrescription> createPrescription(@RequestParam Long doctorId,
                                                       @Valid @RequestBody PrescriptionDTO dto) {
        return Result.success("处方已生成", prescriptionService.create(doctorId, dto));
    }

    @Operation(summary = "查询患者历史病历")
    @GetMapping("/record/patient/{patientId}")
    public Result<List<BizMedicalRecord>> patientRecords(@PathVariable Long patientId) {
        return Result.success(recordService.listByPatient(patientId));
    }
}

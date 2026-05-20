package com.smarthealthcare.service;

import com.smarthealthcare.dto.MedicalRecordDTO;
import com.smarthealthcare.entity.BizMedicalRecord;

import java.util.List;

public interface MedicalRecordService {

    BizMedicalRecord createOrUpdate(Long doctorId, MedicalRecordDTO dto);

    BizMedicalRecord signAndArchive(Long recordId, Long doctorId);

    BizMedicalRecord getById(Long id);

    List<BizMedicalRecord> listByPatient(Long patientId);

    void requestUnlock(Long recordId, Long doctorId);
}

package com.smarthealthcare.service;

import com.smarthealthcare.dto.PrescriptionDTO;
import com.smarthealthcare.entity.BizPrescription;

import java.util.List;

public interface PrescriptionService {

    BizPrescription create(Long doctorId, PrescriptionDTO dto);

    BizPrescription audit(Long prescriptionId, Long pharmacistId, boolean approved, String opinion);

    BizPrescription dispense(Long prescriptionId, Long pharmacistId);

    List<BizPrescription> listByPatient(Long patientId);

    List<BizPrescription> listPendingAudit();
}

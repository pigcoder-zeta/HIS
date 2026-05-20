package com.smarthealthcare.service;

import com.smarthealthcare.entity.BizExamAppointment;
import com.smarthealthcare.entity.BizExamPackage;

import java.util.List;

public interface ExamService {

    List<BizExamPackage> listPackages();

    BizExamAppointment book(Long patientId, Long packageId, String date);

    List<BizExamAppointment> listByPatient(Long patientId);

    void cancel(Long appointmentId, Long patientId);
}

package com.smarthealthcare.service;

import com.smarthealthcare.entity.BizAppointment;
import java.util.List;

public interface AppointmentService {

    BizAppointment book(Long patientId, Long scheduleId);

    void cancel(Long appointmentId, Long patientId);

    List<BizAppointment> listByPatient(Long patientId);

    List<BizAppointment> listTodayByDoctor(Long doctorId);

    void updateStatus(Long appointmentId, Integer status);
}

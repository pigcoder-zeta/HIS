package com.smarthealthcare.dto;

import lombok.Data;

@Data
public class MedicalRecordDTO {

    private Long patientId;
    private Long appointmentId;
    private String chiefComplaint;
    private String presentIllness;
    private String physicalExamination;
    private String auxiliaryExam;
    private String diagnosis;
    private String treatmentPlan;
}

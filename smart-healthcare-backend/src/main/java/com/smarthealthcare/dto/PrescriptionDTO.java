package com.smarthealthcare.dto;

import lombok.Data;
import java.util.List;

@Data
public class PrescriptionDTO {

    private Long recordId;
    private Long patientId;
    private List<PrescriptionItemDTO> items;
}

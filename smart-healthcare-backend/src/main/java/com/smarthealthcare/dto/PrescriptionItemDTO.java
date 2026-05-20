package com.smarthealthcare.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrescriptionItemDTO {

    private Long drugId;
    private String dosage;
    private String usageMethod;
    private Integer quantity;
}

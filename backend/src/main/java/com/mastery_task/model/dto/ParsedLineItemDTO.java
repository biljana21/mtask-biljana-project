package com.mastery_task.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParsedLineItemDTO {
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal taxRate;
}

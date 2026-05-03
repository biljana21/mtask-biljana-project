package com.mastery_task.model.dto;

import com.mastery_task.model.enums.EDocumentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateDocumentRequestDTO {
    private EDocumentType documentType;
    private String supplierName;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String currency;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    private List<ParsedLineItemDTO> lineItems;
}

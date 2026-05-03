package com.mastery_task.model.dto;

import com.mastery_task.model.enums.EDocumentStatus;
import com.mastery_task.model.enums.EDocumentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ParsedDocumentDTO {
    private Integer id;
    private String originalFilename;
    private String storedFilename;
    private String mimeType;
    private EDocumentType documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String supplierName;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private EDocumentStatus status;

    private List<ParsedLineItemDTO> lineItems;
    private List<String> validationIssues;
}
package com.mastery_task.model.dto;

import com.mastery_task.model.enums.EDocumentStatus;
import com.mastery_task.model.enums.EDocumentType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class DocumentResponseDTO {
    private Integer id;
    private String originalFilename;
    private String storedFilename;
    private String mimeType;
    private EDocumentType documentType;
    private String supplierName;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String currency;
    private Double subtotal;
    private Double tax;
    private Double total;
    private EDocumentStatus status;
    private Instant createdDate;
    private Instant updatedAt;

    private List<ParsedLineItemDTO> lineItems;
    private List<String> validationIssues;
}

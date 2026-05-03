package com.mastery_task.mapper;

import com.mastery_task.model.dto.DocumentResponseDTO;
import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.ParsedLineItemDTO;
import com.mastery_task.model.dto.UpdateDocumentRequestDTO;
import com.mastery_task.model.entity.DocumentEntity;
import com.mastery_task.model.entity.LineItemEntity;
import com.mastery_task.model.entity.ValidationIssueEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DocumentMapper {

    public DocumentEntity toEntity(ParsedDocumentDTO dto) {
        DocumentEntity document = new DocumentEntity();

        document.setOriginalFilename(dto.getOriginalFilename());
        document.setStoredFilename(dto.getStoredFilename());
        document.setMimeType(dto.getMimeType());

        document.setDocumentType(dto.getDocumentType());
        document.setSupplier(dto.getSupplierName());
        document.setDocumentNumber(dto.getDocumentNumber());
        document.setIssueDate(dto.getIssueDate());
        document.setDueDate(dto.getDueDate());
        document.setCurrency(dto.getCurrency());

        document.setSubtotal(toDouble(dto.getSubtotal()));
        document.setTax(toDouble(dto.getTax()));
        document.setTotal(toDouble(dto.getTotal()));

        document.setStatus(dto.getStatus());

        mapLineItems(dto, document);
        mapValidationIssues(dto, document);

        return document;
    }

    private void mapLineItems(ParsedDocumentDTO dto, DocumentEntity document) {
        if (dto.getLineItems() == null) return;

        for (ParsedLineItemDTO itemDto : dto.getLineItems()) {
            LineItemEntity item = new LineItemEntity();

            item.setDescription(itemDto.getDescription());
            item.setQuantity(toDouble(itemDto.getQuantity()));
            item.setPrice(toDouble(itemDto.getUnitPrice()));
            item.setTotal(toDouble(itemDto.getLineTotal()));
            item.setTaxRate(toDouble(itemDto.getTaxRate()));

            item.setDocument(document);
            document.getItems().add(item);
        }
    }

    private void mapValidationIssues(ParsedDocumentDTO dto, DocumentEntity document) {
        if (dto.getValidationIssues() == null) return;

        for (String issueMessage : dto.getValidationIssues()) {
            ValidationIssueEntity issue = new ValidationIssueEntity();

            issue.setMessage(issueMessage);
            issue.setResolved(false);
            issue.setDocument(document);

            document.getValidationIssues().add(issue);
        }
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    public DocumentResponseDTO toResponseDto(DocumentEntity entity) {
        DocumentResponseDTO dto = new DocumentResponseDTO();

        dto.setId(entity.getId());
        dto.setOriginalFilename(entity.getOriginalFilename());
        dto.setStoredFilename(entity.getStoredFilename());
        dto.setMimeType(entity.getMimeType());

        dto.setDocumentType(entity.getDocumentType());
        dto.setSupplierName(entity.getSupplier());
        dto.setDocumentNumber(entity.getDocumentNumber());
        dto.setIssueDate(entity.getIssueDate());
        dto.setDueDate(entity.getDueDate());
        dto.setCurrency(entity.getCurrency());

        dto.setSubtotal(entity.getSubtotal());
        dto.setTax(entity.getTax());
        dto.setTotal(entity.getTotal());

        dto.setStatus(entity.getStatus());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setLineItems(entity.getItems() == null
                ? List.of()
                : entity.getItems().stream()
                .map(this::toLineItemDto)
                .toList());

        dto.setValidationIssues(entity.getValidationIssues() == null
                ? List.of()
                : entity.getValidationIssues().stream()
                .map(ValidationIssueEntity::getMessage)
                .toList());

        return dto;
    }

    private ParsedLineItemDTO toLineItemDto(LineItemEntity entity) {
        ParsedLineItemDTO dto = new ParsedLineItemDTO();

        dto.setDescription(entity.getDescription());
        dto.setQuantity(toBigDecimal(entity.getQuantity()));
        dto.setUnitPrice(toBigDecimal(entity.getPrice()));
        dto.setLineTotal(toBigDecimal(entity.getTotal()));
        dto.setTaxRate(toBigDecimal(entity.getTaxRate()));

        return dto;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    public ParsedDocumentDTO toParsedDto(Integer id, UpdateDocumentRequestDTO request) {
        ParsedDocumentDTO dto = new ParsedDocumentDTO();

        dto.setId(id);
        dto.setDocumentType(request.getDocumentType());
        dto.setSupplierName(request.getSupplierName());
        dto.setDocumentNumber(request.getDocumentNumber());
        dto.setIssueDate(request.getIssueDate());
        dto.setDueDate(request.getDueDate());
        dto.setCurrency(request.getCurrency());
        dto.setSubtotal(request.getSubtotal());
        dto.setTax(request.getTax());
        dto.setTotal(request.getTotal());
        dto.setLineItems(request.getLineItems());

        return dto;
    }

    public void updateEntity(DocumentEntity document, ParsedDocumentDTO dto) {
        document.setDocumentType(dto.getDocumentType());
        document.setSupplier(dto.getSupplierName());
        document.setDocumentNumber(dto.getDocumentNumber());
        document.setIssueDate(dto.getIssueDate());
        document.setDueDate(dto.getDueDate());
        document.setCurrency(dto.getCurrency());
        document.setSubtotal(toDouble(dto.getSubtotal()));
        document.setTax(toDouble(dto.getTax()));
        document.setTotal(toDouble(dto.getTotal()));
        document.setStatus(dto.getStatus());

        document.getItems().clear();
        if (dto.getLineItems() != null) {
            for (ParsedLineItemDTO itemDto : dto.getLineItems()) {
                LineItemEntity item = new LineItemEntity();
                item.setDescription(itemDto.getDescription());
                item.setQuantity(toDouble(itemDto.getQuantity()));
                item.setPrice(toDouble(itemDto.getUnitPrice()));
                item.setTotal(toDouble(itemDto.getLineTotal()));
                item.setTaxRate(toDouble(itemDto.getTaxRate()));
                item.setDocument(document);
                document.getItems().add(item);
            }
        }

        document.getValidationIssues().clear();
        if (dto.getValidationIssues() != null) {
            for (String message : dto.getValidationIssues()) {
                ValidationIssueEntity issue = new ValidationIssueEntity();
                issue.setMessage(message);
                issue.setResolved(false);
                issue.setDocument(document);
                document.getValidationIssues().add(issue);
            }
        }
    }
}

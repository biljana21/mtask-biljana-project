package com.mastery_task.service;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.ParsedLineItemDTO;
import com.mastery_task.model.enums.EDocumentType;
import com.mastery_task.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentValidationServiceTest {

    private DocumentRepository documentRepository;
    private DocumentValidationService validationService;

    @BeforeEach
    void setUp() {
        documentRepository = Mockito.mock(DocumentRepository.class);
        validationService = new DocumentValidationService(documentRepository);
    }

    @Test
    void shouldDetectIncorrectTotal() {
        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setDocumentType(EDocumentType.INVOICE);
        doc.setSubtotal(new BigDecimal("645.00"));
        doc.setTax(new BigDecimal("129.00"));
        doc.setTotal(new BigDecimal("800.00"));

        List<String> issues = validationService.validate(doc);

        assertTrue(issues.contains("Incorrect total. Expected 774.00 but found 800.00"));
    }

    @Test
    void shouldDetectIncorrectLineItemTotal() {
        ParsedLineItemDTO item = new ParsedLineItemDTO();
        item.setDescription("Service A");
        item.setQuantity(new BigDecimal("5"));
        item.setUnitPrice(new BigDecimal("129.00"));
        item.setLineTotal(new BigDecimal("600.00"));

        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setDocumentType(EDocumentType.INVOICE);
        doc.setTotal(new BigDecimal("600.00"));
        doc.setLineItems(List.of(item));

        List<String> issues = validationService.validate(doc);

        assertTrue(issues.contains("Invalid line item total for: Service A"));
    }

    @Test
    void shouldDetectIncorrectSubtotalFromLineItems() {
        ParsedLineItemDTO item1 = new ParsedLineItemDTO();
        item1.setDescription("Service A");
        item1.setQuantity(new BigDecimal("5"));
        item1.setUnitPrice(new BigDecimal("129.00"));
        item1.setLineTotal(new BigDecimal("645.00"));

        ParsedLineItemDTO item2 = new ParsedLineItemDTO();
        item2.setDescription("Service B");
        item2.setQuantity(new BigDecimal("2"));
        item2.setUnitPrice(new BigDecimal("10.00"));
        item2.setLineTotal(new BigDecimal("20.00"));

        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setDocumentType(EDocumentType.INVOICE);
        doc.setSubtotal(new BigDecimal("645.00"));
        doc.setTax(new BigDecimal("129.00"));
        doc.setTotal(new BigDecimal("774.00"));
        doc.setLineItems(List.of(item1, item2));

        List<String> issues = validationService.validate(doc);

        assertTrue(issues.contains("Incorrect subtotal. Expected 665.00 but found 645.00"));
    }

    @Test
    void shouldDetectInvalidDueDate() {
        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setDocumentType(EDocumentType.INVOICE);
        doc.setTotal(new BigDecimal("100.00"));
        doc.setIssueDate(LocalDate.of(2026, 5, 10));
        doc.setDueDate(LocalDate.of(2026, 5, 1));

        List<String> issues = validationService.validate(doc);

        assertTrue(issues.contains("Due date cannot be before issue date"));
    }

    @Test
    void shouldDetectDuplicateDocumentNumberOnCreate() {
        when(documentRepository.existsByDocumentNumber("INV-1000"))
                .thenReturn(true);

        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setDocumentType(EDocumentType.INVOICE);
        doc.setDocumentNumber("INV-1000");
        doc.setTotal(new BigDecimal("100.00"));

        List<String> issues = validationService.validate(doc);

        assertTrue(issues.contains("Duplicate document number: INV-1000"));
        verify(documentRepository).existsByDocumentNumber("INV-1000");
    }

    @Test
    void shouldNotDetectDuplicateDocumentNumberForSameDocumentOnUpdate() {
        when(documentRepository.existsByDocumentNumberAndIdNot("INV-1000", 1))
                .thenReturn(false);

        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setId(1);
        doc.setDocumentType(EDocumentType.INVOICE);
        doc.setDocumentNumber("INV-1000");
        doc.setTotal(new BigDecimal("100.00"));

        List<String> issues = validationService.validate(doc);

        assertFalse(issues.contains("Duplicate document number: INV-1000"));
        verify(documentRepository).existsByDocumentNumberAndIdNot("INV-1000", 1);
    }
}

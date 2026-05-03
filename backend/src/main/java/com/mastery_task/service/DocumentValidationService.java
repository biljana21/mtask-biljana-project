package com.mastery_task.service;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.ParsedLineItemDTO;
import com.mastery_task.model.enums.EDocumentType;
import com.mastery_task.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Service
public class DocumentValidationService {

    private final DocumentRepository documentRepository;

    public DocumentValidationService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<String> validate(ParsedDocumentDTO doc) {
        List<String> issues = new ArrayList<>();

        validateRequiredFields(doc, issues);
        validateCurrency(doc, issues);
        validateDates(doc, issues);
        validateLineItems(doc, issues);
        validateSubtotalFromLineItems(doc, issues);
        validateTaxFromLineItems(doc, issues);
        validateTotals(doc, issues);
        validateDuplicateDocumentNumber(doc, issues);

        return issues;
    }

    private void validateRequiredFields(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getDocumentType() == null || doc.getDocumentType() == EDocumentType.UNKNOWN) {
            issues.add("Missing or unknown document type");
        }

        if (doc.getTotal() == null) {
            issues.add("Missing total");
        }
    }

    private void validateDates(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getIssueDate() != null && doc.getDueDate() != null
                && doc.getDueDate().isBefore(doc.getIssueDate())) {
            issues.add("Due date cannot be before issue date");
        }
    }

    private void validateLineItems(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getLineItems() == null) {
            return;
        }

        for (ParsedLineItemDTO item : doc.getLineItems()) {
            if (item.getQuantity() == null || item.getUnitPrice() == null || item.getLineTotal() == null) {
                issues.add("Line item has missing values");
                continue;
            }

            BigDecimal expected = item.getQuantity().multiply(item.getUnitPrice());

            if (expected.compareTo(item.getLineTotal()) != 0) {
                issues.add("Invalid line item total for: " + item.getDescription());
            }
        }
    }

    private void validateTotals(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getSubtotal() == null || doc.getTax() == null || doc.getTotal() == null) {
            return;
        }

        BigDecimal expectedTotal = doc.getSubtotal().add(doc.getTax());

        if (expectedTotal.compareTo(doc.getTotal()) != 0) {
            issues.add("Incorrect total. Expected " + expectedTotal + " but found " + doc.getTotal());
        }
    }

    private void validateDuplicateDocumentNumber(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getDocumentNumber() == null || doc.getDocumentNumber().isBlank()) {
            return;
        }

        boolean exists;

        if (doc.getId() == null) {
            exists = documentRepository.existsByDocumentNumber(doc.getDocumentNumber());
        } else {
            exists = documentRepository.existsByDocumentNumberAndIdNot(
                    doc.getDocumentNumber(),
                    doc.getId()
            );
        }

        if (exists) {
            issues.add("Duplicate document number: " + doc.getDocumentNumber());
        }
    }

    private void validateCurrency(ParsedDocumentDTO doc, List<String> issues) {
        String currency = doc.getCurrency();

        if (currency == null || currency.isBlank()) {
            return; // it is optional field
        }

        String normalized = normalizeCurrency(currency);

        if (!isValidCurrency(normalized)) {
            issues.add("Invalid currency: " + currency);
        } else {
            doc.setCurrency(normalized);
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null) return null;

        currency = currency.trim().toUpperCase();

        return switch (currency) {
            case "$" -> "USD";
            case "€" -> "EUR";
            case "£" -> "GBP";
            case "KM", "BAM" -> "BAM";
            default -> currency;
        };
    }

    private static boolean isValidCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return false;
        }

        try {
            Currency.getInstance(currencyCode.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void validateTaxFromLineItems(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getLineItems() == null || doc.getLineItems().isEmpty() || doc.getTax() == null) {
            return;
        }

        BigDecimal calculatedTax = BigDecimal.ZERO;
        boolean hasAtLeastOneTaxRate = false;

        for (ParsedLineItemDTO item : doc.getLineItems()) {
            if (item.getLineTotal() == null || item.getTaxRate() == null) {
                continue;
            }

            hasAtLeastOneTaxRate = true;

            BigDecimal lineTax = item.getLineTotal()
                    .multiply(item.getTaxRate())
                    .divide(BigDecimal.valueOf(100));

            calculatedTax = calculatedTax.add(lineTax);
        }

        if (!hasAtLeastOneTaxRate) {
            return;
        }

        if (calculatedTax.compareTo(doc.getTax()) != 0) {
            issues.add("Incorrect tax. Expected " + calculatedTax + " but found " + doc.getTax());
        }
    }

    private void validateSubtotalFromLineItems(ParsedDocumentDTO doc, List<String> issues) {
        if (doc.getLineItems() == null || doc.getLineItems().isEmpty() || doc.getSubtotal() == null) {
            return;
        }

        BigDecimal calculatedSubtotal = BigDecimal.ZERO;

        for (ParsedLineItemDTO item : doc.getLineItems()) {
            if (item.getLineTotal() == null) {
                continue;
            }

            calculatedSubtotal = calculatedSubtotal.add(item.getLineTotal());
        }

        if (calculatedSubtotal.compareTo(doc.getSubtotal()) != 0) {
            issues.add("Incorrect subtotal. Expected " + calculatedSubtotal + " but found " + doc.getSubtotal());
        }
    }
}

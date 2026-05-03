package com.mastery_task.util;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.enums.EDocumentType;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TxtDocumentParser {

    public static ParsedDocumentDTO parseTxt(InputStream inputStream, String originalFilename) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setOriginalFilename(originalFilename);

        extractDocumentType(text, doc);
        extractDocumentNumber(text, doc);
        extractSupplier(text, doc);
        extractIssueDate(text, doc);
        extractDueDate(text, doc);
        extractTotalAndCurrency(text, doc);

        return doc;
    }

    private static void extractDocumentType(String text, ParsedDocumentDTO doc) {
        String lower = text.toLowerCase();

        if (lower.contains("purchase order")) {
            doc.setDocumentType(EDocumentType.PURCHASE_ORDER);
        } else if (lower.contains("invoice")) {
            doc.setDocumentType(EDocumentType.INVOICE);
        } else {
            doc.setDocumentType(EDocumentType.UNKNOWN);
        }
    }

    private static void extractDocumentNumber(String text, ParsedDocumentDTO doc) {
        Pattern pattern = Pattern.compile(
                "(Invoice|Purchase Order|PO)\\s*[:#]?\\s*([A-Z0-9-]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            doc.setDocumentNumber(matcher.group(2).trim());
        }
    }

    private static void extractSupplier(String text, ParsedDocumentDTO doc) {
        Pattern pattern = Pattern.compile("Supplier\\s*:?\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            doc.setSupplierName(matcher.group(1).trim());
        }
    }

    private static void extractIssueDate(String text, ParsedDocumentDTO doc) {
        Pattern pattern = Pattern.compile(
                "(Issue Date|Date)\\s*:?\\s*(\\d{4}-\\d{2}-\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            doc.setIssueDate(LocalDate.parse(matcher.group(2)));
        }
    }

    private static void extractDueDate(String text, ParsedDocumentDTO doc) {
        Pattern pattern = Pattern.compile(
                "Due Date\\s*:?\\s*(\\d{4}-\\d{2}-\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            doc.setDueDate(LocalDate.parse(matcher.group(1)));
        }
    }

    private static void extractTotalAndCurrency(String text, ParsedDocumentDTO doc) {
        Pattern pattern = Pattern.compile(
                "Total\\s*:?\\s*([$€£]?\\s*[\\d,.]+)\\s*([A-Z]{3})?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            doc.setTotal(parseMoney(matcher.group(1)));

            if (matcher.group(2) != null) {
                doc.setCurrency(matcher.group(2).trim().toUpperCase());
            } else if (matcher.group(1).contains("$")) {
                doc.setCurrency("$");
            } else if (matcher.group(1).contains("€")) {
                doc.setCurrency("€");
            } else if (matcher.group(1).contains("£")) {
                doc.setCurrency("£");
            }
        }
    }

    private static BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value
                .replace("$", "")
                .replace("€", "")
                .replace("£", "")
                .replace(",", "")
                .trim();

        return new BigDecimal(cleaned);
    }
}

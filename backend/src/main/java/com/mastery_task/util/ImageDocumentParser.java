package com.mastery_task.util;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.enums.EDocumentType;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDocumentParser {

    public static ParsedDocumentDTO parseImage(File imageFile, String originalFilename, String tessDataPath) {
        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        doc.setOriginalFilename(originalFilename);

        String text;

        try {
            Tesseract tesseract = new Tesseract();

            tesseract.setDatapath(tessDataPath);

            text = tesseract.doOCR(imageFile);

        } catch (TesseractException e) {
            doc.setDocumentType(EDocumentType.UNKNOWN);
            return doc;
        }

        extractDocumentType(text, doc);
        extractDocumentNumber(text, doc);
        extractSupplier(text, doc);
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
                "(Invoice|Invoice No:?|Invoice #|Purchase Order|PO)\\s*[:#]?\\s*([A-Z0-9-]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            doc.setDocumentNumber(matcher.group(2).trim());
        }
    }

    private static void extractSupplier(String text, ParsedDocumentDTO doc) {
        Pattern supplierPattern = Pattern.compile("Supplier\\s*:?\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher supplierMatcher = supplierPattern.matcher(text);

        if (supplierMatcher.find()) {
            doc.setSupplierName(supplierMatcher.group(1).trim());
            return;
        }

        Pattern companyPattern = Pattern.compile(
                "Company Name\\s*,?\\s*Ltd\\.?|\\[Company Name\\]|Jehan & Co\\.",
                Pattern.CASE_INSENSITIVE
        );

        Matcher companyMatcher = companyPattern.matcher(text);

        if (companyMatcher.find()) {
            doc.setSupplierName(companyMatcher.group().trim());
        }
    }

    private static void extractTotalAndCurrency(String text, ParsedDocumentDTO doc) {
        Pattern pattern = Pattern.compile(
                "(Total Due|Grand Total|TOTAL Due|Total GBP|Total|Amount Due This Invoice)\\s*[:$€£]?\\s*([\\d,.]+)\\s*([A-Z]{3})?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        BigDecimal foundTotal = null;
        String foundCurrency = null;

        while (matcher.find()) {
            foundTotal = parseMoney(matcher.group(2));

            if (matcher.group(1).toLowerCase().contains("gbp")) {
                foundCurrency = "GBP";
            } else if (matcher.group(3) != null) {
                foundCurrency = matcher.group(3).trim().toUpperCase();
            } else if (matcher.group(0).contains("$")) {
                foundCurrency = "$";
            } else if (matcher.group(0).contains("€")) {
                foundCurrency = "€";
            } else if (matcher.group(0).contains("£")) {
                foundCurrency = "£";
            }
        }

        doc.setTotal(foundTotal);
        doc.setCurrency(foundCurrency);
    }

    private static BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value
                .replace(",", "")
                .replace("$", "")
                .replace("€", "")
                .replace("£", "")
                .trim();

        return new BigDecimal(cleaned);
    }
}

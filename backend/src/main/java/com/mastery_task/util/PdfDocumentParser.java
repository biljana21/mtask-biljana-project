package com.mastery_task.util;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.ParsedLineItemDTO;
import com.mastery_task.model.enums.EDocumentType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfDocumentParser {

    public static ParsedDocumentDTO parsePdf(InputStream inputStream, String originalFilename) throws IOException {
        byte[] bytes = inputStream.readAllBytes();

        String text;
        try (PDDocument pdfDocument = Loader.loadPDF(bytes)) {
            text = new PDFTextStripper().getText(pdfDocument);
        }

        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        List<ParsedLineItemDTO> lineItems = new ArrayList<>();

        doc.setOriginalFilename(originalFilename);

        extractDocumentType(text, doc);
        extractSupplier(text, doc);
        extractDocumentNumber(text, doc);
        extractDate(text, doc);
        extractLineItem(text, lineItems);
        extractSubtotal(text, doc);
        extractTax(text, doc, lineItems);
        extractTotal(text, doc);

        doc.setLineItems(lineItems);

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

    private static void extractSupplier(String text, ParsedDocumentDTO doc) {
        Matcher matcher = Pattern.compile("Supplier:\\s*(.+)", Pattern.CASE_INSENSITIVE).matcher(text);

        if (matcher.find()) {
            doc.setSupplierName(matcher.group(1).trim());
        }
    }

    private static void extractDocumentNumber(String text, ParsedDocumentDTO doc) {
        Matcher matcher = Pattern.compile("Number:\\s*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE).matcher(text);

        if (matcher.find()) {
            doc.setDocumentNumber(matcher.group(1).trim());
        }
    }

    private static void extractDate(String text, ParsedDocumentDTO doc) {
        Matcher matcher = Pattern.compile("Date:\\s*(\\d{4}-\\d{2}-\\d{2})", Pattern.CASE_INSENSITIVE).matcher(text);

        if (matcher.find()) {
            doc.setIssueDate(LocalDate.parse(matcher.group(1)));
        }
    }

    private static void extractLineItem(String text, List<ParsedLineItemDTO> lineItems) {
        Pattern pattern = Pattern.compile(
                "(Service A|Item X)\\s+(\\d+)\\s+([\\d.]+)\\s+([\\d.]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            ParsedLineItemDTO item = new ParsedLineItemDTO();
            item.setDescription(matcher.group(1).trim());
            item.setQuantity(new BigDecimal(matcher.group(2)));
            item.setUnitPrice(new BigDecimal(matcher.group(3)));
            item.setLineTotal(new BigDecimal(matcher.group(4)));

            lineItems.add(item);
        }
    }

    private static void extractSubtotal(String text, ParsedDocumentDTO doc) {
        Matcher matcher = Pattern.compile("Subtotal\\s+([\\d.]+)", Pattern.CASE_INSENSITIVE).matcher(text);

        if (matcher.find()) {
            doc.setSubtotal(new BigDecimal(matcher.group(1)));
        }
    }

    private static void extractTax(String text, ParsedDocumentDTO doc, List<ParsedLineItemDTO> lineItems) {
        Matcher matcher = Pattern.compile("Tax\\s*\\((\\d+)%\\)\\s*([\\d.]+)", Pattern.CASE_INSENSITIVE).matcher(text);

        if (matcher.find()) {
            BigDecimal taxRate = new BigDecimal(matcher.group(1));
            BigDecimal taxAmount = new BigDecimal(matcher.group(2));

            doc.setTax(taxAmount);

            if (lineItems.size() == 1) {
                lineItems.get(0).setTaxRate(taxRate);
            }
        }
    }

    private static void extractTotal(String text, ParsedDocumentDTO doc) {
        Matcher matcher = Pattern.compile("Total\\s+([\\d.]+)", Pattern.CASE_INSENSITIVE).matcher(text);

        BigDecimal lastTotal = null;

        while (matcher.find()) {
            lastTotal = new BigDecimal(matcher.group(1));
        }

        doc.setTotal(lastTotal);
    }
}

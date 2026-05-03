package com.mastery_task.util;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.ParsedLineItemDTO;
import com.mastery_task.model.enums.EDocumentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class CsvDocumentParser {

    public static ParsedDocumentDTO parseCsv(InputStream inputStream, String originalFilename) throws IOException {
        ParsedDocumentDTO doc = new ParsedDocumentDTO();
        List<ParsedLineItemDTO> lineItems = new ArrayList<>();

        doc.setOriginalFilename(originalFilename);
        doc.setDocumentType(EDocumentType.UNKNOWN);
        doc.setTax(BigDecimal.ZERO);

        BigDecimal subtotal = BigDecimal.ZERO;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String header = reader.readLine();

            if (isValidCsvHeader(header)) {
                doc.setDocumentType(EDocumentType.PURCHASE_ORDER);
            }

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",");

                // Skip rows that do not match expected structure (cannot be parsed reliably)
                if (columns.length != 4) {
                    continue;
                }

                try {
                    String description = columns[0].trim();
                    BigDecimal quantity = new BigDecimal(columns[1].trim());
                    BigDecimal unitPrice = new BigDecimal(columns[2].trim());
                    BigDecimal lineTotal = new BigDecimal(columns[3].trim());

                    ParsedLineItemDTO item = new ParsedLineItemDTO();
                    item.setDescription(description);
                    item.setQuantity(quantity);
                    item.setUnitPrice(unitPrice);
                    item.setLineTotal(lineTotal);

                    lineItems.add(item);
                    subtotal = subtotal.add(lineTotal);

                } catch (Exception e) {
                    // Ignore malformed rows (e.g. invalid numeric values); these rows are excluded from processing
                }
            }
        }

        doc.setLineItems(lineItems);
        doc.setSubtotal(subtotal);
        doc.setTotal(subtotal);

        return doc;
    }

    private static boolean isValidCsvHeader(String header) {
        if (header == null || header.isBlank()) {
            return false;
        }

        String[] columns = header.toLowerCase().split(",");

        return columns.length == 4
                && columns[0].trim().equals("desc")
                && columns[1].trim().equals("qty")
                && columns[2].trim().equals("price")
                && columns[3].trim().equals("total");
    }
}

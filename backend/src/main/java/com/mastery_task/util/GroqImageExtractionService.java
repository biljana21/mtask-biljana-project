package com.mastery_task.util;

import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.groq.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.List;


@Service
public class GroqImageExtractionService {

    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1";
    private static final String MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GroqImageExtractionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create(GROQ_BASE_URL);
    }

    public ParsedDocumentDTO extractFromImage(MultipartFile file) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing Groq API key");
        }

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUrl = "data:" + file.getContentType() + ";base64," + base64Image;

        GroqChatRequest request = new GroqChatRequest(
                MODEL,
                List.of(
                        new GroqMessage(
                                "user",
                                List.of(
                                        new GroqContent("text", buildPrompt(), null),
                                        new GroqContent("image_url", null, new ImageUrl(dataUrl))
                                )
                        )
                ),
                0.0
        );

        GroqResponse response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(GroqResponse.class);

        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()
                || response.getChoices().get(0).getMessage() == null
                || response.getChoices().get(0).getMessage().getContent() == null) {
            throw new IllegalStateException("Empty Groq response");
        }

        String json = cleanJson(response.getChoices().get(0).getMessage().getContent());

        ParsedDocumentDTO dto = objectMapper.readValue(json, ParsedDocumentDTO.class);
        dto.setOriginalFilename(file.getOriginalFilename());
        dto.setMimeType(file.getContentType());

        return dto;
    }

    private String buildPrompt() {
        return """
                You are extracting structured data from a business document image.

                Return ONLY valid JSON.
                Do not include markdown.
                Do not include explanations.
                Do not invent missing fields.

                JSON structure:
                {
                  "documentType": "INVOICE" | "PURCHASE_ORDER" | "UNKNOWN",
                  "supplierName": string or null,
                  "documentNumber": string or null,
                  "issueDate": "YYYY-MM-DD" or null,
                  "dueDate": "YYYY-MM-DD" or null,
                  "currency": string or null,
                  "subtotal": number or null,
                  "tax": number or null,
                  "total": number or null,
                  "lineItems": [
                    {
                      "description": string,
                      "quantity": number or null,
                      "unitPrice": number or null,
                      "lineTotal": number or null,
                      "taxRate": number or null
                    }
                  ]
                }

                Rules:
                - If a field is not visible, use null.
                - Currency must be ISO code, for example USD, EUR, GBP, BAM, AED.
                - Convert dates to YYYY-MM-DD when possible.
                - Return numbers without currency symbols.
                - For line items, use visible quantity, unit price, total amount, and tax/VAT rate.
                - If line item tax/VAT percentage is visible, return it as taxRate.
                - If line item tax/VAT percentage is not visible, use null.
                - If the image is not an invoice or purchase order, set documentType to UNKNOWN.
                """;
    }

    private String cleanJson(String content) {
        if (content == null) {
            return "{}";
        }

        String cleaned = content.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }
}

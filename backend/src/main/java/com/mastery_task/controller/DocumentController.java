package com.mastery_task.controller;

import com.mastery_task.model.dto.CurrencyTotalResponseDTO;
import com.mastery_task.model.dto.DocumentResponseDTO;
import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.UpdateDocumentRequestDTO;
import com.mastery_task.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin("*")
@Tag(name = "Documents", description = "Document processing API")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Upload and process a document",
            description = "Uploads a file (CSV, TXT, PDF, or Image), extracts data, validates it, and returns parsed result.")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ParsedDocumentDTO> uploadCsv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        ParsedDocumentDTO parsedDocument = documentService.uploadDocument(file);

        return ResponseEntity.ok(parsedDocument);
    }

    @Operation(summary = "Update document",
            description = "Allows manual correction of extracted document data. Re-validates and updates status.")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(
            @PathVariable Integer id,
            @RequestBody UpdateDocumentRequestDTO request
    ) {
        return ResponseEntity.ok(documentService.updateDocument(id, request));
    }


    @Operation(summary = "Get paginated documents",
            description = "Returns a paginated list of processed documents with extracted data and validation issues.")
    @GetMapping
    public ResponseEntity<Page<DocumentResponseDTO>> getDocuments(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Pageable pagination = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return ResponseEntity.ok(documentService.getDocuments(pagination));
    }

    @Operation(summary = "Get document by ID",
            description = "Returns full document details including line items and validation issues.")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @Operation(summary = "Preview document file",
            description = "Displays the document file inline in the browser.")
    @GetMapping("/{id}/file/preview")
    public ResponseEntity<Resource> previewDocument(@PathVariable Integer id) throws IOException {

        DocumentResponseDTO doc = documentService.getDocumentById(id);
        Resource resource = documentService.getDocumentFile(id);

        MediaType mediaType = MediaType.parseMediaType(doc.getMimeType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + doc.getOriginalFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Download document file",
            description = "Forces download of the original document file.")
    @GetMapping("/{id}/file/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer id) throws IOException {

        DocumentResponseDTO doc = documentService.getDocumentById(id);
        Resource resource = documentService.getDocumentFile(id);

        MediaType mediaType = MediaType.parseMediaType(doc.getMimeType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                .body(resource);
    }

    @Operation(
            summary = "Get totals grouped by currency",
            description = "Returns total document amounts grouped by currency. Documents without currency are grouped as UNKNOWN."
    )
    @GetMapping("/dashboard/totals-by-currency")
    public ResponseEntity<List<CurrencyTotalResponseDTO>> getTotalsGroupedByCurrency() {
        return ResponseEntity.ok(documentService.getTotalsGroupedByCurrency());
    }

}

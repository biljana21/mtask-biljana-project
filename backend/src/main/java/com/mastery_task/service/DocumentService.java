package com.mastery_task.service;

import com.mastery_task.exception.ResourceNotFoundException;
import com.mastery_task.mapper.DocumentMapper;
import com.mastery_task.model.dto.CurrencyTotalResponseDTO;
import com.mastery_task.model.dto.DocumentResponseDTO;
import com.mastery_task.model.dto.ParsedDocumentDTO;
import com.mastery_task.model.dto.UpdateDocumentRequestDTO;
import com.mastery_task.model.entity.DocumentEntity;
import com.mastery_task.model.enums.EDocumentStatus;
import com.mastery_task.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.mastery_task.repository.DocumentRepository;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    @Value("${tesseract.datapath}")
    private String tessDataPath;

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final GroqImageExtractionService groqImageExtractionService;
    private final DocumentValidationService documentValidationService;
    private final DocumentMapper documentMapper;

    @Value("${parser.image.strategy}")
    private String imageStrategy;

    public DocumentService(DocumentRepository documentRepository, FileStorageService fileStorageService, GroqImageExtractionService groqImageExtractionService, DocumentValidationService documentValidationService, DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.groqImageExtractionService = groqImageExtractionService;
        this.documentValidationService = documentValidationService;
        this.documentMapper = documentMapper;
    }

    public Page<DocumentResponseDTO> getDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(documentMapper::toResponseDto);
    }

    public DocumentResponseDTO getDocumentById(Integer id) {
        return documentRepository.findById(id)
                .map(documentMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    public Resource getDocumentFile(Integer id) throws IOException {

        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        File file = fileStorageService.getFile(document.getStoredFilename());

        if (!file.exists()) {
            throw new ResourceNotFoundException("File not found");
        }

        return new UrlResource(file.toPath().toUri());
    }

    public ParsedDocumentDTO uploadDocument(MultipartFile file) throws IOException {
        String storedFilename = fileStorageService.storeFile(file);

        ParsedDocumentDTO parsedDocument = parseDocument(file, storedFilename);

        List<String> issues = documentValidationService.validate(parsedDocument);

        parsedDocument.setValidationIssues(issues);
        parsedDocument.setStatus(issues.isEmpty()
                ? EDocumentStatus.VALIDATED
                : EDocumentStatus.NEEDS_REVIEW);

        parsedDocument.setStoredFilename(storedFilename);
        //parsedDocument.setFilePath(fileStorageService.getUploadDir() + storedFilename);
        parsedDocument.setMimeType(file.getContentType());

        DocumentEntity documentEntity = documentMapper.toEntity(parsedDocument);

        documentRepository.save(documentEntity);

        parsedDocument.setId(documentEntity.getId());

        return parsedDocument;
    }

    private ParsedDocumentDTO parseDocument(MultipartFile file, String storedFilename) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("Invalid file");
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".csv")) {
            return CsvDocumentParser.parseCsv(
                    file.getInputStream(),
                    filename
            );
        }

        if (lowerFilename.endsWith(".txt")) {
            return TxtDocumentParser.parseTxt(
                    file.getInputStream(),
                    filename
            );
        }

        if (lowerFilename.endsWith(".pdf")) {
            return PdfDocumentParser.parsePdf(file.getInputStream(), filename);
        }

        if (lowerFilename.endsWith(".png")
                || lowerFilename.endsWith(".jpg")
                || lowerFilename.endsWith(".jpeg")) {

            if ("GROQ".equalsIgnoreCase(imageStrategy)) {
                return groqImageExtractionService.extractFromImage(file);
            }

            if ("OCR".equalsIgnoreCase(imageStrategy)) {
                File storedFile = fileStorageService.getFile(storedFilename);

                return ImageDocumentParser.parseImage(
                        storedFile,
                        filename,
                        tessDataPath
                );
            }

            throw new IllegalStateException("Unknown image parser strategy: " + imageStrategy);
        }

        throw new IllegalArgumentException("Unsupported file type");
    }

    @Transactional
    public DocumentResponseDTO updateDocument(Integer id, UpdateDocumentRequestDTO request) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        ParsedDocumentDTO dto = documentMapper.toParsedDto(id, request);

        List<String> issues = documentValidationService.validate(dto);

        dto.setValidationIssues(issues);
        dto.setStatus(issues.isEmpty()
                ? EDocumentStatus.VALIDATED
                : EDocumentStatus.NEEDS_REVIEW);

        documentMapper.updateEntity(document, dto);

        DocumentEntity saved = documentRepository.save(document);

        return documentMapper.toResponseDto(saved);
    }

    public List<CurrencyTotalResponseDTO> getTotalsGroupedByCurrency() {
        return documentRepository.getTotalsGroupedByCurrency();
    }
}

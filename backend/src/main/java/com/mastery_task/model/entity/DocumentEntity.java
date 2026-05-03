package com.mastery_task.model.entity;

import com.mastery_task.model.enums.EDocumentStatus;
import com.mastery_task.model.enums.EDocumentType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document")
@EntityListeners(AuditingEntityListener.class)
@Data
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String originalFilename;
    private String storedFilename;
    private String mimeType;

    @Enumerated(EnumType.STRING)
    private EDocumentType documentType;

    private String supplier;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String currency;
    private Double subtotal;
    private Double tax;
    private Double total;

    @Enumerated(EnumType.STRING)
    private EDocumentStatus status;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    private Instant updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValidationIssueEntity> validationIssues = new ArrayList<>();
}
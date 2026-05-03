package com.mastery_task.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "validation_issue")
@Data
public class ValidationIssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String message;
    private Boolean resolved = false;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;
}

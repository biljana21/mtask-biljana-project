package com.mastery_task.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "line_item")
@Data
public class LineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;
    private Double quantity;
    private Double price; //unit price
    private Double total;
    private Double taxRate;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;
}

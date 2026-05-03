package com.mastery_task.model.dto.groq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroqContent {
    private String type;
    private String text;
    private ImageUrl image_url;
}

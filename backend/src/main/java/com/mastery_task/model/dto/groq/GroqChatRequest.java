package com.mastery_task.model.dto.groq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroqChatRequest {
    private String model;
    private List<GroqMessage> messages;
    private double temperature;
}

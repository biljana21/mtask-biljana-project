package com.mastery_task.service;

import com.mastery_task.model.entity.LineItemEntity;
import org.springframework.stereotype.Service;
import com.mastery_task.repository.LineItemRepository;

import java.util.List;

@Service
public class LineItemService {

    private final LineItemRepository lineItemRepository;

    public LineItemService(LineItemRepository lineItemRepository) {
        this.lineItemRepository = lineItemRepository;
    }

    public List<LineItemEntity> getAll() {
        return lineItemRepository.findAll();
    }
}

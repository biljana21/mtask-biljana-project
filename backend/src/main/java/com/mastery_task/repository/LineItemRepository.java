package com.mastery_task.repository;

import com.mastery_task.model.entity.LineItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineItemRepository extends JpaRepository<LineItemEntity, Integer> {
}

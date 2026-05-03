package com.mastery_task.repository;

import com.mastery_task.model.entity.ValidationIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationIssueRepository extends JpaRepository<ValidationIssueEntity, Integer> {
}

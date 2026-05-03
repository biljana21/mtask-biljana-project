package com.mastery_task.repository;

import com.mastery_task.model.dto.CurrencyTotalResponseDTO;
import com.mastery_task.model.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, Integer id);

    @Query("""
       SELECT new com.mastery_task.model.dto.CurrencyTotalResponseDTO(
           COALESCE(d.currency, 'UNKNOWN'),
           SUM(d.total)
       )
       FROM DocumentEntity d
       WHERE d.total IS NOT NULL
       GROUP BY COALESCE(d.currency, 'UNKNOWN')
       """)
    List<CurrencyTotalResponseDTO> getTotalsGroupedByCurrency();
}

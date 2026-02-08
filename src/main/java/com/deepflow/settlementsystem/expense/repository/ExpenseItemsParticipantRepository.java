package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.ExpenseItemsParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseItemsParticipantRepository extends JpaRepository<ExpenseItemsParticipant, Long> {
  List<ExpenseItemsParticipant> findByItemItemId(Long itemId);
}

package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {
    List<ExpenseParticipant> findByExpenseExpenseId(Long expenseId);
}

package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.Expense;
import com.deepflow.settlementsystem.expense.entity.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {
    void deleteByExpense(Expense expense);
    List<ExpenseItem> findByExpenseExpenseId(Long expenseId);
}

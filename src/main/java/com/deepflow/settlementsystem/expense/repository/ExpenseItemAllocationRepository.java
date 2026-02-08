package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.ExpenseAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseItemAllocationRepository extends JpaRepository<ExpenseAllocation, Long> {
}

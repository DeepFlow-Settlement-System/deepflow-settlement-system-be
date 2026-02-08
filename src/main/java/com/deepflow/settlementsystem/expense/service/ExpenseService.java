package com.deepflow.settlementsystem.expense.service;

import com.deepflow.settlementsystem.expense.dto.CreateExpenseRequest;
import com.deepflow.settlementsystem.expense.dto.CreateExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseTotalResponse;
import java.time.LocalDate;

public interface ExpenseService {

  CreateExpenseResponse createExpense(Long groupId, CreateExpenseRequest request);

  GroupExpenseTotalResponse getGroupTotal(Long groupId);

  GroupExpenseResponse getGroupExpenses(Long groupId, LocalDate startDate, LocalDate endDate);
}

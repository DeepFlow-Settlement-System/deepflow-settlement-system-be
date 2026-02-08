package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByGroup_GroupId(Long groupId);

    List<Expense> findAllByGroup_GroupIdAndSpentAtBetween(Long groupId, LocalDateTime start, LocalDateTime end);

    List<Expense> findAllByGroup_GroupIdAndSpentAtGreaterThanEqual(Long groupId, LocalDateTime start);

    List<Expense> findAllByGroup_GroupIdAndSpentAtLessThanEqual(Long groupId, LocalDateTime end);
}

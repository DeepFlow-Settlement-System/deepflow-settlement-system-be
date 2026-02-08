package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}

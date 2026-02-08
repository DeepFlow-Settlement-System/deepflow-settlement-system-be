package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.ExpenseAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseItemAllocationRepository extends JpaRepository<ExpenseAllocation, Long> {
    
    @Query("SELECT ea FROM ExpenseAllocation ea " +
           "LEFT JOIN FETCH ea.sender " +
           "LEFT JOIN FETCH ea.receiver " +
           "LEFT JOIN FETCH ea.group " +
           "LEFT JOIN FETCH ea.expense " +
           "WHERE ea.sender.id = :userId OR ea.receiver.id = :userId")
    List<ExpenseAllocation> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ea FROM ExpenseAllocation ea " +
           "LEFT JOIN FETCH ea.sender " +
           "LEFT JOIN FETCH ea.receiver " +
           "LEFT JOIN FETCH ea.group " +
           "LEFT JOIN FETCH ea.expense " +
           "WHERE ea.allocationId = :allocationId")
    Optional<ExpenseAllocation> findByIdWithRelations(@Param("allocationId") Long allocationId);
    
    @Query("SELECT ea FROM ExpenseAllocation ea " +
           "LEFT JOIN FETCH ea.sender " +
           "LEFT JOIN FETCH ea.receiver " +
           "LEFT JOIN FETCH ea.group " +
           "LEFT JOIN FETCH ea.expense " +
           "WHERE ea.sender.id = :senderId AND ea.receiver.id = :receiverId")
    List<ExpenseAllocation> findBySenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}

package com.deepflow.settlementsystem.expense.entity;

import com.deepflow.settlementsystem.group.entity.Group;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.deepflow.settlementsystem.user.entity.User;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "expense_allocations")
public class ExpenseAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ExpenseItem item;

    @ManyToOne
    @JoinColumn(name = "sender_id")     // 돈을 보낼 사람
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")   // 돈을 받을 사람
    private User receiver;

    @Column(name = "share_amount")
    private Integer shareAmount;        // 낼 금액

    @Column(name = "status")
    private SettlementStatus status;              // UNSETTLED, REQUESTED, COMPLETED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

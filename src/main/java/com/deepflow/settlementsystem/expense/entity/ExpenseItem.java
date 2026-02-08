package com.deepflow.settlementsystem.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "expense_items")
public class ExpenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense; // 지출 내역 '한 건'

    @Column(name = "item_name", length = 200)
    private String itemName; // 영수증의 구매 항목 이름

    @Column(name = "line_amount")
    private Integer lineAmount; // 항목별 금액

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "item")
    private List<ExpenseAllocation> allocations = new ArrayList<>();

    @OneToMany(mappedBy = "item")
    private List<ExpenseItemsParticipant>  participants = new ArrayList<>();
}

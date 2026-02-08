package com.deepflow.settlementsystem.group.entity;

import com.deepflow.settlementsystem.expense.entity.Expense;
import com.deepflow.settlementsystem.expense.entity.ExpenseAllocation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToOne(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Room room;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Group(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setRoom(Room room) {
        this.room = room;
    }


    // 추가자: 양재혁
    // ================================================================================================
    @OneToMany(mappedBy = "group")  // 현재 그룹이 가진, 지출들
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "group")  // 현재 그룹이 가진, 송금 내역들
    private List<ExpenseAllocation> expenseAllocations = new ArrayList<>();
    // ================================================================================================
}

package com.deepflow.settlementsystem.expense.entity;

import com.deepflow.settlementsystem.group.entity.Group;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.deepflow.settlementsystem.user.entity.User;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "expense_id")
  private Long expenseId;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private Group group;

  @ManyToOne
  @JoinColumn(name = "payer_user_id")
  private User payerUser;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "receipt_image_id")
  private Receipt receipt; // 영수증 원본 + OCR 결과

  @Column(name = "spent_at")
  private LocalDateTime spentAt; // 지출일

  @Column(name = "title", length = 100)
  private String title; // 가게명 OR 지출명

  @Column(name = "total_amount")
  private Integer totalAmount; // 영수증 총합 가격 OR N빵 총금액

  @Enumerated(EnumType.STRING)
  @Column(name = "settlement_type", length = 20, nullable = false)
  private SettlementType settlementType; // 정산 방법: N빵 | 품목별

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "expense")
  private List<ExpenseItem> items = new ArrayList<>();

  @OneToMany(mappedBy = "expense")
  private List<ExpenseParticipant> participants = new ArrayList<>();

  @OneToMany(mappedBy = "expense") // 이 지출로 인한 송금내역
  private List<ExpenseAllocation> allocations = new ArrayList<>();
}

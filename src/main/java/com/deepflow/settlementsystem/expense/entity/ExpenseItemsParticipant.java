package com.deepflow.settlementsystem.expense.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.deepflow.settlementsystem.user.entity.User;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "expense_items_participant")
public class ExpenseItemsParticipant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "item_id")
  private ExpenseItem item;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

}

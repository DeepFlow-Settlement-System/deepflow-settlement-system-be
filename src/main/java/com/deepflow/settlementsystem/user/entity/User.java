package com.deepflow.settlementsystem.user.entity;

import com.deepflow.settlementsystem.expense.entity.Expense;
import com.deepflow.settlementsystem.expense.entity.ExpenseAllocation;
import com.deepflow.settlementsystem.expense.entity.ExpenseItemsParticipant;
import com.deepflow.settlementsystem.expense.entity.ExpenseParticipant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "kakaoId", nullable = false, unique = true)
    private Long kakaoId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "kakao_pay_suffix", unique = true)
    private String kakaoPaySuffix;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }


    // 추가자 : 양재혁
    // ==================================================================================================
    // 지출관련 관계 정의입니다.
    @OneToMany(mappedBy = "user")       // 이 유저가 참여한 지출들
    private List<ExpenseParticipant> expenseParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "sender")     // 이 유저가 돈을 보낼 내역들
    private List<ExpenseAllocation> sendList = new ArrayList<>();

    @OneToMany(mappedBy = "receiver")   // 이 유저가 돈을 받을 내역들
    private List<ExpenseAllocation> receiveList = new ArrayList<>();

    @OneToMany(mappedBy = "payerUser")  // 이 유저가 결제자인 지출들
    private List<Expense> paidExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "user")       // (항목당 지출인 경우) 이 유저가 구매에 참여한 항목들
    private List<ExpenseItemsParticipant> itemsParticipants = new ArrayList<>();
    // ==================================================================================================
}

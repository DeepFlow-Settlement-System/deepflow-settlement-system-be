package com.deepflow.settlementsystem.settlement.entity;

import com.deepflow.settlementsystem.group.entity.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlements", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Long userId; // 사용자 ID

    @Column(nullable = false)
    private Long receiveAmount; // 받을 금액

    @Column(nullable = false)
    private Long sendAmount; // 보낼 금액

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime calculatedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Settlement(Room room, Long userId, Long receiveAmount, Long sendAmount) {
        this.room = room;
        this.userId = userId;
        this.receiveAmount = receiveAmount;
        this.sendAmount = sendAmount;
    }

    public void updateAmounts(Long receiveAmount, Long sendAmount) {
        this.receiveAmount = receiveAmount;
        this.sendAmount = sendAmount;
    }
}

package com.deepflow.settlementsystem.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    private Group group;

    @Column(nullable = false, unique = true, length = 50)
    private String inviteCode; // 초대 코드

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 만료 시간 (다음날 00시 또는 재생성 시점 + 24시간)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Room(Group group) {
        this.group = group;
        this.inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        // 다음날 00시로 설정 (스케줄러가 매일 00시에 재생성)
        this.expiresAt = LocalDate.now().plusDays(1).atStartOfDay();
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    // 초대 코드 재생성 메서드
    public void regenerateInviteCode() {
        this.inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

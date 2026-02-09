package com.deepflow.settlementsystem.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(nullable = true)
    private LocalDateTime expiresAt; // 만료 시간 (사용하지 않음, 하위 호환성을 위해 유지)

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
        // expiresAt는 사용하지 않지만 DB 제약조건을 위해 최대값 설정
        this.expiresAt = LocalDateTime.now().plusYears(1);
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}

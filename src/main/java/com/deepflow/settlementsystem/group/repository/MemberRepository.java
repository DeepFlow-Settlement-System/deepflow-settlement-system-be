package com.deepflow.settlementsystem.group.repository;

import com.deepflow.settlementsystem.group.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    List<Member> findByRoomId(Long roomId);

    @Query("SELECT m FROM Member m WHERE m.room.id = :roomId AND m.user.id = :userId")
    Optional<Member> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}

package com.deepflow.settlementsystem.settlement.repository;

import com.deepflow.settlementsystem.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    
    List<Settlement> findByRoomId(Long roomId);

    @Query("SELECT s FROM Settlement s WHERE s.room.id = :roomId AND s.userId = :userId")
    Optional<Settlement> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Settlement s WHERE s.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}

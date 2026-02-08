package com.deepflow.settlementsystem.group.repository;

import com.deepflow.settlementsystem.group.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByInviteCode(String inviteCode);
    
    @Query("SELECT r FROM Room r " +
           "LEFT JOIN FETCH r.group " +
           "WHERE r.inviteCode = :inviteCode")
    Optional<Room> findByInviteCodeWithGroup(@Param("inviteCode") String inviteCode);
}

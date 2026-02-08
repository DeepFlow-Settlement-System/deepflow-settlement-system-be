package com.deepflow.settlementsystem.group.repository;

import com.deepflow.settlementsystem.group.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByInviteCode(String inviteCode);
}

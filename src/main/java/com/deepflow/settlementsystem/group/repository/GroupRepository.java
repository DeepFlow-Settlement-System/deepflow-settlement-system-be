package com.deepflow.settlementsystem.group.repository;

import com.deepflow.settlementsystem.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT DISTINCT g FROM Group g " +
           "LEFT JOIN FETCH g.room r " +
           "JOIN Member m ON m.room.id = r.id " +
           "WHERE m.user.id = :userId")
    List<Group> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT g FROM Group g " +
           "LEFT JOIN FETCH g.room " +
           "WHERE g.id = :groupId")
    Optional<Group> findByIdWithRoom(@Param("groupId") Long groupId);
}

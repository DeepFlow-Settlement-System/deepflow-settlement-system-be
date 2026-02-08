package com.deepflow.settlementsystem.group.scheduler;

import com.deepflow.settlementsystem.group.entity.Room;
import com.deepflow.settlementsystem.group.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteCodeScheduler {

    private final RoomRepository roomRepository;

    // 한국 시간 기준 매일 00시에 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void regenerateInviteCodes() {
        log.info("초대 코드 재생성 스케줄러 시작 - {}", LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        
        List<Room> rooms = roomRepository.findAll();
        int regeneratedCount = 0;
        
        for (Room room : rooms) {
            room.regenerateInviteCode();
            regeneratedCount++;
        }
        
        roomRepository.saveAll(rooms);
        
        log.info("초대 코드 재생성 완료 - 총 {}개 Room의 초대 코드 재생성", regeneratedCount);
    }
}

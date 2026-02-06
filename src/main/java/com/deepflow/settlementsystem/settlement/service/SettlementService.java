package com.deepflow.settlementsystem.settlement.service;

// import com.deepflow.settlementsystem.expense.entity.Expense;
// import com.deepflow.settlementsystem.expense.entity.ExpenseParticipant;
// import com.deepflow.settlementsystem.expense.repository.ExpenseParticipantRepository;
// import com.deepflow.settlementsystem.expense.repository.ExpenseRepository;
import com.deepflow.settlementsystem.group.entity.Member;
import com.deepflow.settlementsystem.group.entity.Room;
import com.deepflow.settlementsystem.group.repository.MemberRepository;
import com.deepflow.settlementsystem.group.repository.RoomRepository;
// import com.deepflow.settlementsystem.kakao.repository.KakaoTokenRepository;
// import com.deepflow.settlementsystem.kakao.service.KakaoService;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementResponse;
import com.deepflow.settlementsystem.settlement.entity.Settlement;
import com.deepflow.settlementsystem.settlement.repository.SettlementRepository;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;
    // private final ExpenseRepository expenseRepository;
    // private final ExpenseParticipantRepository expenseParticipantRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    // private final KakaoService kakaoService;
    private final UserRepository userRepository;
    // private final KakaoTokenRepository kakaoTokenRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public SettlementListResponse calculateSettlement(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // 기존 정산 결과 삭제
        settlementRepository.deleteByRoomId(roomId);

        // 방의 모든 멤버 조회
        List<Member> members = memberRepository.findByRoomId(roomId);
        if (members.isEmpty()) {
            throw new IllegalArgumentException("방에 멤버가 없습니다.");
        }

        // 방의 모든 지출 조회
        // List<Expense> expenses = expenseRepository.findByRoomId(roomId);
        // if (expenses.isEmpty()) {
        //     throw new IllegalArgumentException("지출 내역이 없습니다.");
        // }

        // 사용자별 금액 계산
        Map<Long, Long> receiveAmounts = new HashMap<>(); // 받을 금액
        Map<Long, Long> sendAmounts = new HashMap<>(); // 보낼 금액

        // 초기화
        for (Member member : members) {
            receiveAmounts.put(member.getUser().getId(), 0L);
            sendAmounts.put(member.getUser().getId(), 0L);
        }

        // 각 지출에 대해 정산 계산
        // for (Expense expense : expenses) {
        //     Long payerId = expense.getPayerId();
        //     Long amount = expense.getAmount();
        //
        //     // 지출 대상자 조회
        //     List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
        //     if (participants.isEmpty()) {
        //         continue;
        //     }
        //
        //     // 1/N 정산: 참여자 수로 나누기
        //     Long perPersonAmount = amount / participants.size();
        //     Long remainder = amount % participants.size(); // 나머지는 결제자가 부담
        //
        //     // 결제자는 본인 부담금을 제외한 나머지를 받을 금액으로 계산
        //     Long payerShare = perPersonAmount + remainder;
        //     receiveAmounts.put(payerId, receiveAmounts.get(payerId) + amount - payerShare);
        //
        //     // 각 참여자는 본인 부담금을 보낼 금액으로 계산
        //     for (ExpenseParticipant participant : participants) {
        //         Long participantId = participant.getUserId();
        //         if (!participantId.equals(payerId)) {
        //             sendAmounts.put(participantId, sendAmounts.get(participantId) + perPersonAmount);
        //         } else {
        //             // 결제자는 본인 부담금만 보낼 금액에 추가
        //             sendAmounts.put(participantId, sendAmounts.get(participantId) + payerShare);
        //         }
        //     }
        // }

        // 정산 결과 저장
        List<Settlement> settlements = members.stream()
                .map(member -> {
                    Long userId = member.getUser().getId();
                    Long receiveAmount = receiveAmounts.getOrDefault(userId, 0L);
                    Long sendAmount = sendAmounts.getOrDefault(userId, 0L);

                    Settlement settlement = Settlement.builder()
                            .room(room)
                            .userId(userId)
                            .receiveAmount(receiveAmount)
                            .sendAmount(sendAmount)
                            .build();
                    return settlementRepository.save(settlement);
                })
                .collect(Collectors.toList());

        // 응답 생성
        List<SettlementResponse> settlementResponses = settlements.stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());

        return SettlementListResponse.builder()
                .roomId(roomId)
                .settlements(settlementResponses)
                .build();
    }

    public SettlementListResponse getSettlement(Long roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        List<Settlement> settlements = settlementRepository.findByRoomId(roomId);
        if (settlements.isEmpty()) {
            throw new IllegalArgumentException("정산 결과가 없습니다. 먼저 정산을 계산해주세요.");
        }

        List<SettlementResponse> settlementResponses = settlements.stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());

        return SettlementListResponse.builder()
                .roomId(roomId)
                .settlements(settlementResponses)
                .build();
    }

    private SettlementResponse toSettlementResponse(Settlement settlement) {
        Long netAmount = settlement.getReceiveAmount() - settlement.getSendAmount();
        return SettlementResponse.builder()
                .id(settlement.getId())
                .roomId(settlement.getRoom().getId())
                .userId(settlement.getUserId())
                .receiveAmount(settlement.getReceiveAmount())
                .sendAmount(settlement.getSendAmount())
                .netAmount(netAmount)
                .calculatedAt(settlement.getCalculatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }

    @Transactional
    public void sendSettlementMessages(Long roomId, List<Long> receiverUserIds, Long senderUserId) {
        // 정산 결과 조회
        List<Settlement> settlements = settlementRepository.findByRoomId(roomId);
        if (settlements.isEmpty()) {
            throw new IllegalArgumentException("정산 결과가 없습니다. 먼저 정산을 계산해주세요.");
        }

        // 보낼 사람의 카카오 토큰 확인
        // kakaoTokenRepository.findByUserId(senderUserId)
        //         .orElseThrow(() -> new IllegalArgumentException("카카오 토큰을 찾을 수 없습니다."));

        // 각 수신자에게 메시지 전송
        for (Long receiverUserId : receiverUserIds) {
            Settlement settlement = settlements.stream()
                    .filter(s -> s.getUserId().equals(receiverUserId))
                    .findFirst()
                    .orElse(null);

            if (settlement == null) {
                continue; // 해당 사용자의 정산 결과가 없으면 스킵
            }

            // User 조회하여 카카오 UUID 가져오기
            // User receiver = userRepository.findById(receiverUserId)
            //         .orElse(null);

            // if (receiver == null || receiver.getKakaoUuid() == null) {
            //     continue; // 카카오 UUID가 없으면 스킵
            // }

            // 메시지 내용 생성
            // String message = String.format(
            //         "정산 요청이 있습니다.\n받을 금액: %,d원\n보낼 금액: %,d원",
            //         settlement.getReceiveAmount(),
            //         settlement.getSendAmount()
            // );

            // 결제 링크 생성 (간단한 예시)
            // String paymentLink = baseUrl + "/api/payments?settlementId=" + settlement.getId();

            // 카카오 메시지 전송
            // String[] receiverUuids = {receiver.getKakaoUuid()};
            // kakaoService.sendMessage(senderUserId, receiverUuids, message, paymentLink);
        }
    }
}

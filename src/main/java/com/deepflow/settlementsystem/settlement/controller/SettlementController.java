package com.deepflow.settlementsystem.settlement.controller;

import com.deepflow.settlementsystem.settlement.dto.request.SettlementSendRequest;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementSummaryResponse;
import com.deepflow.settlementsystem.settlement.service.SettlementService;
import com.deepflow.settlementsystem.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Settlement", description = "정산 관련 API")
@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @Operation(
            summary = "정산 목록 조회",
            description = "현재 로그인한 사용자와 관련된 모든 정산 목록을 조회합니다. (송금해야 할 내역과 받아야 할 내역 모두 포함)"
    )
    @GetMapping
    public ResponseEntity<SettlementListResponse> getSettlementList(
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal @NotNull User user) {
        SettlementListResponse response = settlementService.getSettlementList(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "정산 상태 조회",
            description = "특정 정산의 상세 상태를 조회합니다. sender 또는 receiver만 조회 가능합니다."
    )
    @GetMapping("/{allocationId}")
    public ResponseEntity<SettlementResponse> getSettlementStatus(
            @Parameter(description = "정산 ID (allocationId)", required = true, example = "1")
            @PathVariable Long allocationId,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal @NotNull User user) {
        SettlementResponse response = settlementService.getSettlementStatus(allocationId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "정산 요청 메시지 전송",
            description = "카카오톡으로 정산 요청 메시지를 전송합니다. 돈을 받는 사람(receiver)만 요청할 수 있으며, " +
                    "상태를 선택하여 요청하거나 통합적으로 한번에 요청할 수 있습니다."
    )
    @PostMapping("/send")
    public ResponseEntity<Void> sendSettlementMessage(
            @Parameter(description = "정산 요청 정보", required = true)
            @Valid @RequestBody SettlementSendRequest request,
            @Parameter(description = "현재 로그인한 사용자 (receiver)", required = true, hidden = true)
            @AuthenticationPrincipal @NotNull User receiver) {
        settlementService.sendSettlementMessage(
                request.getTargetUserId(),
                request.getStatuses(),
                receiver.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @Operation(
            summary = "사용자 간 정산 요약 조회",
            description = "로그인한 사용자와 특정 사용자 간의 정산 건을 상태별로 묶어서 금액을 조회합니다."
    )
    @GetMapping("/summary/{targetUserId}")
    public ResponseEntity<SettlementSummaryResponse> getSettlementSummary(
            @Parameter(description = "상대방 사용자 ID", required = true, example = "1")
            @PathVariable Long targetUserId,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal @NotNull User user) {
        SettlementSummaryResponse response = settlementService.getSettlementSummary(user.getId(), targetUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "정산 완료 처리",
            description = "정산을 완료 처리합니다. 돈을 받는 사람(receiver)만 완료 처리할 수 있으며, " +
                    "REQUESTED 상태인 정산만 COMPLETED로 변경할 수 있습니다."
    )
    @PatchMapping("/{allocationId}/complete")
    public ResponseEntity<Void> completeSettlement(
            @Parameter(description = "정산 ID (allocationId)", required = true, example = "1")
            @PathVariable Long allocationId,
            @Parameter(description = "현재 로그인한 사용자 (receiver)", required = true, hidden = true)
            @AuthenticationPrincipal @NotNull User receiver) {
        settlementService.completeSettlement(allocationId, receiver.getId());
        return ResponseEntity.noContent().build();
    }
}

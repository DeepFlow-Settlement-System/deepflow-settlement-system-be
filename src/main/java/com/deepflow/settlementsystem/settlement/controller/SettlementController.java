package com.deepflow.settlementsystem.settlement.controller;

import com.deepflow.settlementsystem.settlement.dto.request.SettlementSendRequest;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementResponse;
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
                    "메시지를 받는 사람(sender)에게 카카오페이 송금 링크가 포함된 메시지가 전송됩니다."
    )
    @PostMapping("/send")
    public ResponseEntity<Void> sendSettlementMessage(
            @Parameter(description = "정산 요청 정보", required = true)
            @Valid @RequestBody SettlementSendRequest request,
            @Parameter(description = "현재 로그인한 사용자 (receiver)", required = true, hidden = true)
            @AuthenticationPrincipal @NotNull User receiver) {
        settlementService.sendSettlementMessage(
                request.getAllocationId(),
                receiver.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
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

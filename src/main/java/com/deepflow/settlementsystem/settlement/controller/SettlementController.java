package com.deepflow.settlementsystem.settlement.controller;

import com.deepflow.settlementsystem.settlement.dto.request.SettlementMessageRequest;
import com.deepflow.settlementsystem.settlement.service.SettlementService;
import com.deepflow.settlementsystem.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "정산 요청 메시지 전송", description = "카카오톡으로 정산 요청 메시지를 전송합니다.")
    @PostMapping("/send")
    public ResponseEntity<Void> sendSettlementMessage(
            @Valid @RequestBody SettlementMessageRequest request,
            @AuthenticationPrincipal @NotNull User sender) {
        settlementService.sendSettlementMessage(
                sender.getId(),
                request.getReceiverUserId(),
                request.getAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

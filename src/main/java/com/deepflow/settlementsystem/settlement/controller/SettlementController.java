package com.deepflow.settlementsystem.settlement.controller;

import com.deepflow.settlementsystem.settlement.dto.request.SettlementMessageRequest;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.service.SettlementService;
import com.deepflow.settlementsystem.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/rooms/{roomId}/calculate")
    public ResponseEntity<SettlementListResponse> calculateSettlement(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user) {
        SettlementListResponse response = settlementService.calculateSettlement(roomId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<SettlementListResponse> getSettlement(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user) {
        SettlementListResponse response = settlementService.getSettlement(roomId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms/{roomId}/send-messages")
    public ResponseEntity<Void> sendSettlementMessages(
            @PathVariable Long roomId,
            @Valid @RequestBody SettlementMessageRequest request,
            @AuthenticationPrincipal User user) {
        settlementService.sendSettlementMessages(roomId, request.getReceiverUserIds(), user.getId());
        return ResponseEntity.ok().build();
    }
}

package com.deepflow.settlementsystem.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupExpenseResponse {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "지출 목록")
    private List<ExpenseResponse> expenses;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseResponse {
        @Schema(description = "지출 ID", example = "10")
        private Long expenseId;

        @Schema(description = "지출 제목/가게명", example = "커피값")
        private String title;

        @Schema(description = "지출 날짜", example = "2026-02-01T12:00:00")
        private LocalDateTime spentAt;

        @Schema(description = "총 금액", example = "7000")
        private Integer totalAmount;

        @Schema(description = "정산 타입", example = "N_BBANG")
        private String settlementType;

        @Schema(description = "결제자 사용자 ID", example = "3")
        private Long payerUserId;

        @Schema(description = "영수증 ID", example = "1", nullable = true)
        private Long receiptId;

        @Schema(description = "지출 참여자 목록")
        private List<ParticipantResponse> participants;

        @Schema(description = "품목 목록(ITEMIZED일 때만)")
        private List<ExpenseItemResponse> items;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseItemResponse {
        @Schema(description = "품목 ID", example = "100")
        private Long itemId;

        @Schema(description = "품목명", example = "사과")
        private String itemName;

        @Schema(description = "품목 금액", example = "4000")
        private Integer lineAmount;

        @Schema(description = "품목 참여자 목록")
        private List<ItemParticipantResponse> participants;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemParticipantResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;
    }
}

package com.deepflow.settlementsystem.expense.dto;

import com.deepflow.settlementsystem.expense.entity.SettlementType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateExpenseRequest {

    @Schema(description = "지출 제목/가게명", example = "커피값")
    private String title; // 가게명

    @Schema(description = "지출 날짜", example = "2026-02-01T12:00:00")
    private LocalDateTime spentAt; // 지출등록일

    @Schema(description = "결제자 사용자 ID", example = "3")
    private Long payerUserId; // 결제자

    @Schema(description = "영수증 ID(선택)", example = "1", nullable = true)
    private Long receiptImageId = null; // 영수증 사진 id(선택)

    @Schema(description = "정산 타입(N_BBANG 또는 ITEMIZED)", example = "N_BBANG")
    private SettlementType settlementType; // 정산 타입 (n빵 OR 품목별)

    @Schema(description = "품목 리스트(ITEMIZED일 때 사용)")
    private List<Item> items; // 품목 리스트

    @Schema(description = "결제 참여자 목록")
    private List<Participant> participants; // 결제 참여자

    @Schema(description = "N빵일 때 총 금액(콤마 포함 가능)", example = "7,000")
    private String totalAmount; // n빵인 경우 총가격

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Participant {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
      @Schema(description = "품목명", example = "사과")
      private String itemName;    // 품목명

      @Schema(description = "품목 금액(콤마 포함 가능)", example = "4,000")
      private String price;      // 항목 가격

      @Schema(description = "품목 참여자 목록")
      private List<Participant> itemParticipants; // 각 품목의 결제 참여자
    }
}

package com.deepflow.settlementsystem.expense.controller;

import com.deepflow.settlementsystem.expense.dto.CreateExpenseRequest;
import com.deepflow.settlementsystem.expense.dto.CreateExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseTotalResponse;
import com.deepflow.settlementsystem.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Expense", description =
        "지출 등록/조회 API<br>" +
                "사용자의 영수증 첨부가 있을 경우 흐름:<br>" +
                "1) 영수증 사진 업로드 (receiptId 반환)<br>" +
                "2) receiptId로 영수증 상태 조회<br>" +
                "3) 상태가 SUCCESS면 영수증 결과 조회<br>" +
                "4) OCR 결과로 지출 등록 항목 자동 채우기"
)
public class ExpenseController {

  private final ExpenseService expenseService;

  @Operation(
          summary = "지출 등록",
          description = "그룹 내 지출을 등록합니다. N빵은 totalAmount만 사용하고, 품목별은 items를 사용합니다.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = CreateExpenseRequest.class),
                          examples = {
                                  @ExampleObject(
                                          name = "N_BBANG 예시",
                                          value = "{\n  \"title\": \"커피값\",\n  \"spentAt\": \"2026-02-01T12:00:00\",\n  \"payerUserId\": 3,\n  \"receiptImageId\": 1,\n  \"settlementType\": \"N_BBANG\",\n  \"participants\": [\n    {\"userId\": 1},\n    {\"userId\": 2},\n    {\"userId\": 3}\n  ],\n  \"totalAmount\": \"7,000\"\n}"
                                  ),
                                  @ExampleObject(
                                          name = "ITEMIZED 예시",
                                          value = "{\n  \"title\": \"마트\",\n  \"spentAt\": \"2026-02-01T12:00:00\",\n  \"payerUserId\": 3,\n  \"receiptImageId\": 1,\n  \"settlementType\": \"ITEMIZED\",\n  \"items\": [\n    {\n      \"itemName\": \"사과\",\n      \"price\": \"4,000\",\n      \"itemParticipants\": [{\"userId\": 1}, {\"userId\": 2}]\n    },\n    {\n      \"itemName\": \"당근\",\n      \"price\": \"3,000\",\n      \"itemParticipants\": [{\"userId\": 2}, {\"userId\": 3}]\n    }\n  ],\n  \"participants\": [\n    {\"userId\": 1},\n    {\"userId\": 2},\n    {\"userId\": 3}\n  ],\n  \"totalAmount\": \"7,000\"\n}"
                                  )
                          }
                  )
          )
  )
  @PostMapping("/groups/{groupId}/expenses")
  public ResponseEntity<CreateExpenseResponse> createExpense( // 지출 등록
          @PathVariable Long groupId,
          @RequestBody CreateExpenseRequest request
  ) {
      CreateExpenseResponse response = expenseService.createExpense(groupId, request);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/expenses/{groupId}/total") // 그룹별 총 지출 금액
  @Operation(
          summary = "그룹 총 지출 조회",
          description = "groupId에 해당하는 지출들의 totalAmount 합계를 반환합니다."
  )
  public ResponseEntity<GroupExpenseTotalResponse> getGroupTotal(@PathVariable Long groupId) {
      return ResponseEntity.ok(expenseService.getGroupTotal(groupId));
  }

  // 그룹 지출내역 조회
  @GetMapping("/groups/{groupId}/expenses")
  @Operation(
          summary = "그룹 지출내역 조회",
          description = "지출 + 참여자 + (품목별인 경우) 품목/품목참여자 정보를 묶어서 반환합니다."
  )
  public ResponseEntity<GroupExpenseResponse> getExpenses(@PathVariable Long groupId){
    return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
  }


}

package com.deepflow.settlementsystem.expense.service;

import com.deepflow.settlementsystem.common.NumberParser;
import com.deepflow.settlementsystem.expense.dto.CreateExpenseRequest;
import com.deepflow.settlementsystem.expense.dto.CreateExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseTotalResponse;
import com.deepflow.settlementsystem.expense.entity.*;
import com.deepflow.settlementsystem.expense.repository.*;
import com.deepflow.settlementsystem.group.entity.Group;
import com.deepflow.settlementsystem.group.repository.GroupRepository;
import com.deepflow.settlementsystem.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.deepflow.settlementsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
  
  private final GroupRepository groupRepository;
  private final ExpenseRepository expenseRepository;
  private final ExpenseItemRepository expenseItemRepository;
  private final ExpenseParticipantRepository expenseParticipantRepository;
  private final ExpenseItemsParticipantRepository expenseItemsParticipantRepository;
  private final ReceiptRepository receiptRepository;
  private final NumberParser numberParser;
  private final UserRepository userRepository;
  private final ExpenseItemAllocationRepository expenseItemAllocationRepository;

  @Override
  @Transactional
  public CreateExpenseResponse createExpense(Long groupId, CreateExpenseRequest request) {
    // [지출 등록]
    /*
      1. N빵 -> ExpenseItem에 데이터 저장 X, expense.totalAmount가 총 금액
      2. 품목별
     */

    LocalDateTime now = LocalDateTime.now();

    // 그룹 찾기
    Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

    // 결제자 정보 찾기
    Long payerUserId = request.getPayerUserId();
    if (payerUserId == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payerUserId is required");
    }
    User payerUser = userRepository.findById(payerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payer user not found"));


    // 지출 등록
    Expense expense = new Expense();

    expense.setGroup(group);
    expense.setPayerUser(payerUser);
    expense.setSpentAt(request.getSpentAt());
    expense.setTitle(request.getTitle());

    Integer totalAmount = numberParser.parseInt(request.getTotalAmount());
    if (totalAmount == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total amount is required");
    }
    expense.setTotalAmount(totalAmount); // 총 금액

    // 영수증 연결 (선택)
    if (request.getReceiptImageId() != null) {
      Receipt receipt = receiptRepository.findById(request.getReceiptImageId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found"));
      expense.setReceipt(receipt);
    }

    // 지출 타입 저장
    SettlementType settlementType = request.getSettlementType();
    if (settlementType == null || (settlementType != SettlementType.N_BBANG && settlementType != SettlementType.ITEMIZED)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "settlement type is required");
    }
    expense.setSettlementType(request.getSettlementType());

    expense.setCreatedAt(now);
    expense.setUpdatedAt(now);

    Expense savedExpense = expenseRepository.save(expense);


    // 품목별 지출인 경우 데이터 저장
    if (settlementType == settlementType.ITEMIZED) {
      // 1. 각 항목별 저장
      List<CreateExpenseRequest.Item> items = request.getItems();
      List<ExpenseItem> expenseItems = new ArrayList<>();

      for (CreateExpenseRequest.Item item : items) {
        ExpenseItem expenseItem = new ExpenseItem();
        expenseItem.setExpense(savedExpense);
        expenseItem.setItemName(item.getItemName());

        Integer itemPrice = numberParser.parseInt(item.getPrice());
        if (itemPrice == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "item price is required");
        }
        expenseItem.setLineAmount(itemPrice);

        expenseItem.setCreatedAt(now);
        expenseItem.setUpdatedAt(now);

        // 1.2. 각 항목의 결제 참여자 저장
        // ========================================================================================================================
        List<CreateExpenseRequest.Participant> itemParticipants = item.getItemParticipants();
        List<ExpenseItemsParticipant> expenseItemsParticipants = new ArrayList<>();

        for (CreateExpenseRequest.Participant participant : itemParticipants) {
          ExpenseItemsParticipant expenseItemsParticipant = new ExpenseItemsParticipant();
          expenseItemsParticipant.setItem(expenseItem);

          User user = userRepository.findById(participant.getUserId())
                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "expense item user not found"));
          expenseItemsParticipant.setUser(user);

          expenseItemsParticipants.add(expenseItemsParticipant);
        }

        expenseItemsParticipantRepository.saveAll(expenseItemsParticipants);
        // ========================================================================================================================

        expenseItems.add(expenseItem);

      }

      expenseItemRepository.saveAll(expenseItems);
    }


    // 지출 참여자 등록 (ExpenseParticipant)
    List<CreateExpenseRequest.Participant> expenseParticipants = emptyIfNull(request.getParticipants());

    List<ExpenseParticipant> participants = new ArrayList<>();
    if (!expenseParticipants.isEmpty()) {


        for (CreateExpenseRequest.Participant expenseParticipant : expenseParticipants) {
            Long userId = expenseParticipant.getUserId();
            if (userId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "participant.userId is required");
            }

            ExpenseParticipant participant = new ExpenseParticipant();
              participant.setExpense(savedExpense);
              participant.setUser(userRepository.getReferenceById(userId));
            participants.add(participant);
        }
        expenseParticipantRepository.saveAll(participants);
    }


    // 각 참여자당 결제자에게 얼마를 줘야할지 계산
    // N빵인 경우
    // =========================================================================================================================
    if (savedExpense.getSettlementType() == SettlementType.N_BBANG) {

      Integer total = savedExpense.getTotalAmount();
      if (total == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalAmount is required for N_BBANG");
      }

      int count = participants.size();
      if (count == 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "participants are required for N_BBANG");
      }

      int baseShare = total / count;
      int remainder = total % count; // 나머지 잔돈

      User payer = savedExpense.getPayerUser(); // 결제자

      List<ExpenseAllocation> allocations = new ArrayList<>();
      for (ExpenseParticipant participant : participants) {
        User sender = participant.getUser(); // 송금자

        if (sender == payer) { // 결제자가 자신에게 보내는 경우는 제외
          continue;
        }

        ExpenseAllocation expenseAllocation = new ExpenseAllocation();
        expenseAllocation.setGroup(savedExpense.getGroup());
        expenseAllocation.setExpense(savedExpense);
        expenseAllocation.setItem(null);
        expenseAllocation.setSender(sender);
        expenseAllocation.setReceiver(payer);
        expenseAllocation.setShareAmount(baseShare);
        expenseAllocation.setStatus(SettlementStatus.UNSETTLED);
        expenseAllocation.setCreatedAt(now);

        allocations.add(expenseAllocation);
      }

      if (remainder != 0) { // 가격이 남을 경우, 모든 인원이 나머지를 지불
        for (ExpenseAllocation expenseAllocation : allocations) {
          expenseAllocation.setShareAmount(baseShare + remainder);
        }
      }

      expenseItemAllocationRepository.saveAll(allocations);

    }
    // 항목별인 경우
    // =========================================================================================================================
    else if (savedExpense.getSettlementType() == SettlementType.ITEMIZED) {
      User payer = savedExpense.getPayerUser(); // 결제자

      List<ExpenseItem> items = expenseItemRepository.findByExpenseExpenseId(savedExpense.getExpenseId()); // 각 항목들
      for (ExpenseItem item : items) {

        Integer lineAmount = item.getLineAmount(); // 각 항목의 가격

        // 각 항목의 구매 참여자
        List<ExpenseItemsParticipant> itemParticipants = expenseItemsParticipantRepository.findByItemItemId(item.getItemId());

        int count = itemParticipants.size();

        int baseShare = lineAmount / count;
        int remainder = lineAmount % count;

        List<ExpenseAllocation> allocations = new ArrayList<>();
        for (ExpenseItemsParticipant itemParticipant : itemParticipants) {

          User sender = itemParticipant.getUser();

          if (sender == payer) {
            continue;
          }

          ExpenseAllocation expenseAllocation = new ExpenseAllocation();
          expenseAllocation.setGroup(savedExpense.getGroup());
          expenseAllocation.setExpense(savedExpense);
          expenseAllocation.setItem(item);
          expenseAllocation.setSender(sender);
          expenseAllocation.setReceiver(payer);
          expenseAllocation.setShareAmount(baseShare);
          expenseAllocation.setStatus(SettlementStatus.UNSETTLED);
          expenseAllocation.setCreatedAt(now);

          allocations.add(expenseAllocation);
        }

        if (remainder != 0) { // 가격이 남을 경우, 모든 인원이 나머지를 지불
          for (ExpenseAllocation expenseAllocation : allocations) {
            expenseAllocation.setShareAmount(baseShare + remainder);
          }
        }

        expenseItemAllocationRepository.saveAll(allocations);

      }
    }

    return new CreateExpenseResponse(savedExpense.getExpenseId());
  }

  private static <T> List<T> emptyIfNull(List<T> list) {
      return list == null ? List.of() : list;
  }

  @Override
  @Transactional(readOnly = true)
  public GroupExpenseTotalResponse getGroupTotal(Long groupId) {
      groupRepository.findById(groupId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
      long totalAmount = expenseRepository.findAllByGroup_Id(groupId).stream()
              .map(Expense::getTotalAmount)
              .filter(Objects::nonNull)
              .mapToLong(Integer::longValue)
              .sum();
      return new GroupExpenseTotalResponse(groupId, totalAmount);
  }

  @Override
  @Transactional(readOnly = true)
  public GroupExpenseResponse getGroupExpenses(Long groupId, LocalDate startDate, LocalDate endDate) { // 그룹의 총 지출내역 조회
      groupRepository.findById(groupId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

      if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
      }

      LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
      LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

      // 그룹에 속한 모든 지출을 조회해서, 연관 테이블을 JSON 형태로 묶어 반환
      List<Expense> expenses;
      if (startDateTime != null && endDateTime != null) {
          expenses = expenseRepository.findAllByGroup_IdAndSpentAtBetween(groupId, startDateTime, endDateTime);
      } else if (startDateTime != null) {
          expenses = expenseRepository.findAllByGroup_IdAndSpentAtGreaterThanEqual(groupId, startDateTime);
      } else if (endDateTime != null) {
          expenses = expenseRepository.findAllByGroup_IdAndSpentAtLessThanEqual(groupId, endDateTime);
      } else {
          expenses = expenseRepository.findAllByGroup_Id(groupId);
      }
      List<GroupExpenseResponse.ExpenseResponse> expenseResponses = new ArrayList<>();

      for (Expense expense : expenses) {
          // 지출 참여자 목록
          List<GroupExpenseResponse.ParticipantResponse> participantResponses = new ArrayList<>();
          List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseExpenseId(expense.getExpenseId());
          for (ExpenseParticipant participant : participants) {
              participantResponses.add(new GroupExpenseResponse.ParticipantResponse(
                      participant.getUser().getId()
              ));
          }

          List<GroupExpenseResponse.ExpenseItemResponse> itemResponses = new ArrayList<>();
          if (expense.getSettlementType() == SettlementType.ITEMIZED) {
              // 품목별 정산일 때만 항목과 항목 참여자를 포함
              List<ExpenseItem> items = expenseItemRepository.findByExpenseExpenseId(expense.getExpenseId());
              for (ExpenseItem item : items) {
                  List<GroupExpenseResponse.ItemParticipantResponse> itemParticipantResponses = new ArrayList<>();
                  List<ExpenseItemsParticipant> itemParticipants =
                          expenseItemsParticipantRepository.findByItemItemId(item.getItemId());
                  for (ExpenseItemsParticipant itemParticipant : itemParticipants) {
                      itemParticipantResponses.add(new GroupExpenseResponse.ItemParticipantResponse(
                              itemParticipant.getUser().getId()
                      ));
                  }

                  itemResponses.add(new GroupExpenseResponse.ExpenseItemResponse(
                          item.getItemId(),
                          item.getItemName(),
                          item.getLineAmount(),
                          itemParticipantResponses
                  ));
              }
          }

          Long receiptId = expense.getReceipt() != null ? expense.getReceipt().getId() : null;
          Long payerUserId = expense.getPayerUser() != null ? expense.getPayerUser().getId() : null;

          // 지출 1건의 응답 DTO 구성
          expenseResponses.add(new GroupExpenseResponse.ExpenseResponse(
                  expense.getExpenseId(),
                  expense.getTitle(),
                  expense.getSpentAt(),
                  expense.getTotalAmount(),
                  expense.getSettlementType().name(),
                  payerUserId,
                  receiptId,
                  participantResponses,
                  itemResponses
          ));
      }

      return new GroupExpenseResponse(groupId, expenseResponses);
  }
}

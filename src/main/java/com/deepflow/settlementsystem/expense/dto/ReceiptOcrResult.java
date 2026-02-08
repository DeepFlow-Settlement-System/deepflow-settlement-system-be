package com.deepflow.settlementsystem.expense.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceiptOcrResult(
        List<Image> images
)
{

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Image(
          String inferResult,
          Receipt receipt
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Receipt(
          Result result
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Result(
          StoreInfo storeInfo,
          List<SubResults> subResults,
          TotalPrice totalPrice
  ) {}

  // 1. 가게명 추출
  // ========================================================================================
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record StoreInfo (
          Name name
  ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name (
            Formatted formatted
    ) {}

      @JsonIgnoreProperties(ignoreUnknown = true)
      public record Formatted ( // 공유하여 사용
              String value
      ) {}

  // 2. 품목 리스트 추출
  // ========================================================================================
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SubResults(
          List<Item> items
  ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            ItemName name,
            Count count,
            ItemPrice price
    ){}

      @JsonIgnoreProperties(ignoreUnknown = true)
      public record ItemName (
              Formatted formatted
      ) {}

      @JsonIgnoreProperties(ignoreUnknown = true)
      public record Count (
              Formatted formatted
      ) {}

      @JsonIgnoreProperties(ignoreUnknown = true)
      public record ItemPrice (
              PriceDetail price,
              PriceDetail unitPrice
      ){}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PriceDetail (
                Formatted formatted
        ) {}

  // 3. 영수증 총액 추출
  // ========================================================================================
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TotalPrice(
          Price price
  ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Price (
       Formatted formatted
    ){}

  // 편하게 꺼내는 메서드들
  // ========================================================================================
  public String storeName(){
    if (images == null || images.isEmpty()) return "";
    if (images.get(0).receipt() == null) return "";
    var result = images.get(0).receipt().result();

    if (result == null || result.storeInfo() == null || result.storeInfo().name() == null) return "";
    var f = result.storeInfo().name().formatted();

    return f != null ? f.value() : "";
  }

  public int totalAmount(){
    if (images == null || images.isEmpty()) return 0;
    if (images.get(0).receipt() == null) return 0;
    var result = images.get(0).receipt().result();

    if (result == null || result.totalPrice() == null || result.totalPrice().price() == null) return 0;
    var f = result.totalPrice().price().formatted();

    if (f == null || f.value() == null) return 0;
    String digits = f.value().replaceAll("[^0-9]", "");
    if (digits.isBlank()) return 0;
    return Integer.parseInt(digits);
  }


}

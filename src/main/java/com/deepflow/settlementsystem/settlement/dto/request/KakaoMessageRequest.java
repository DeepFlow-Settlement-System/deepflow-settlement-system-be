package com.deepflow.settlementsystem.settlement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class KakaoMessageRequest {
    @JsonProperty("object_type")
    private String objectType;
    
    @JsonProperty("content")
    private Content content;
    
    @JsonProperty("item_content")
    private ItemContent itemContent;
    
    @JsonProperty("buttons")
    private List<Button> buttons;
    
    @Getter
    @Builder
    public static class Content {
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("image_url")
        private String imageUrl;
        
        @JsonProperty("image_width")
        private Integer imageWidth;
        
        @JsonProperty("image_height")
        private Integer imageHeight;
        
        @JsonProperty("link")
        private Link link;
    }
    
    @Getter
    @Builder
    public static class Link {
        @JsonProperty("web_url")
        private String webUrl;
        
        @JsonProperty("mobile_web_url")
        private String mobileWebUrl;
    }
    
    @Getter
    @Builder
    public static class ItemContent {
        @JsonProperty("profile_text")
        private String profileText;
        
        @JsonProperty("items")
        private List<Item> items;
        
        @JsonProperty("sum")
        private String sum;
        
        @JsonProperty("sum_op")
        private String sumOp;
    }
    
    @Getter
    @Builder
    public static class Item {
        @JsonProperty("item")
        private String item;
        
        @JsonProperty("item_op")
        private String itemOp;
    }
    
    @Getter
    @Builder
    public static class Button {
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("link")
        private Link link;
    }
}

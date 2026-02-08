package com.deepflow.settlementsystem.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomJoinRequest {

    @NotBlank
    private String inviteCode;
}

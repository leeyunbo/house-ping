package com.yunbok.houseping.config.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverResponse {

    @JsonProperty("resultcode")
    private String resultCode;

    private String message;

    private NaverUserInfo response;

    @Getter
    @NoArgsConstructor
    public static class NaverUserInfo {
        private String id;
        private String email;
        private String name;
    }
}

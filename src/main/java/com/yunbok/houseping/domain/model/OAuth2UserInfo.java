package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private String naverId;
    private String email;
    private String name;
}

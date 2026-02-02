package com.yunbok.houseping.infrastructure.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunbok.houseping.domain.model.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverUserInfoExtractor implements OAuth2UserInfoExtractor {

    private static final String REGISTRATION_ID = "naver";

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String registrationId) {
        return REGISTRATION_ID.equals(registrationId);
    }

    @Override
    public OAuth2UserInfo extract(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        NaverResponse naverResponse = objectMapper.convertValue(
                oAuth2User.getAttributes(),
                NaverResponse.class
        );

        NaverResponse.NaverUserInfo userInfo = naverResponse.getResponse();

        return OAuth2UserInfo.builder()
                .naverId(userInfo.getId())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .build();
    }
}

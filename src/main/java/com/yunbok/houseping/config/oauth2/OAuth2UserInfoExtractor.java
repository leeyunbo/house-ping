package com.yunbok.houseping.config.oauth2;

import com.yunbok.houseping.config.oauth2.OAuth2UserInfo;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {

    boolean supports(String registrationId);

    OAuth2UserInfo extract(OAuth2User oAuth2User, OAuth2UserRequest userRequest);
}

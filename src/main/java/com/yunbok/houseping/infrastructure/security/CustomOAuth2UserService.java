package com.yunbok.houseping.infrastructure.security;

import com.yunbok.houseping.domain.model.OAuth2UserInfo;
import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.port.in.AuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthenticationUseCase authenticationUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                .naverId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .build();

        User user = authenticationUseCase.processOAuth2Login(userInfo);

        return new CustomOAuth2User(user, attributes);
    }
}

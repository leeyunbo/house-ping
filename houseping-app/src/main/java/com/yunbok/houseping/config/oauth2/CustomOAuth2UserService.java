package com.yunbok.houseping.config.oauth2;

import com.yunbok.houseping.config.oauth2.OAuth2UserInfo;
import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.service.auth.AuthenticationService;
import com.yunbok.houseping.config.oauth2.OAuth2UserInfoExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthenticationService authenticationUseCase;
    private final List<OAuth2UserInfoExtractor> extractors;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfoExtractor extractor = extractors.stream()
                .filter(e -> e.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        "지원하지 않는 OAuth2 제공자입니다: " + registrationId));

        OAuth2UserInfo userInfo = extractor.extract(oAuth2User, userRequest);
        User user = authenticationUseCase.processOAuth2Login(userInfo);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}

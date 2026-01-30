package com.yunbok.houseping.domain.port.in;

import com.yunbok.houseping.domain.model.OAuth2UserInfo;
import com.yunbok.houseping.domain.model.User;

public interface AuthenticationUseCase {
    User processOAuth2Login(OAuth2UserInfo userInfo);
}

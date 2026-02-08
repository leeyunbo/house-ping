package com.yunbok.houseping.infrastructure.security;

import com.yunbok.houseping.config.oauth2.CustomOAuth2User;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.domain.UserRole;
import com.yunbok.houseping.core.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomOAuth2User - OAuth2 사용자 Principal")
class CustomOAuth2UserTest {

    @Nested
    @DisplayName("getAuthorities() - 권한 조회")
    class GetAuthorities {

        @Test
        @DisplayName("MASTER 역할이면 ROLE_MASTER 권한을 반환한다")
        void returnsMasterRoleAuthority() {
            // given
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("마스터")
                    .role(UserRole.MASTER)
                    .status(UserStatus.ACTIVE)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, new HashMap<>());

            // when
            Collection<? extends GrantedAuthority> authorities = oAuth2User.getAuthorities();

            // then
            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_MASTER");
        }

        @Test
        @DisplayName("USER 역할이면 ROLE_USER 권한을 반환한다")
        void returnsUserRoleAuthority() {
            // given
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.PENDING)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, new HashMap<>());

            // when
            Collection<? extends GrantedAuthority> authorities = oAuth2User.getAuthorities();

            // then
            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("getName() - 이름 조회")
    class GetName {

        @Test
        @DisplayName("사용자 이름을 반환한다")
        void returnsUserName() {
            // given
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("홍길동")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, new HashMap<>());

            // when
            String name = oAuth2User.getName();

            // then
            assertThat(name).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("getAttributes() - 속성 조회")
    class GetAttributes {

        @Test
        @DisplayName("생성자에서 전달받은 속성을 반환한다")
        void returnsAttributes() {
            // given
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("response", Map.of("id", "naver-123", "name", "사용자"));

            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, attributes);

            // when
            Map<String, Object> result = oAuth2User.getAttributes();

            // then
            assertThat(result).containsKey("response");
        }
    }

    @Nested
    @DisplayName("getUser() - 사용자 조회")
    class GetUser {

        @Test
        @DisplayName("래핑된 User 도메인 객체를 반환한다")
        void returnsWrappedUser() {
            // given
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, new HashMap<>());

            // when
            User result = oAuth2User.getUser();

            // then
            assertThat(result).isEqualTo(user);
            assertThat(result.getNaverId()).isEqualTo("naver-123");
        }
    }
}

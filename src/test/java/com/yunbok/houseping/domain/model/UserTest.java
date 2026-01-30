package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User - 사용자 도메인 모델")
class UserTest {

    @Nested
    @DisplayName("isActive() - 활성 상태 확인")
    class IsActive {

        @Test
        @DisplayName("ACTIVE 상태이면 true를 반환한다")
        void returnsTrueWhenActive() {
            // given
            User user = User.builder()
                    .status(UserStatus.ACTIVE)
                    .build();

            // when & then
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태이면 false를 반환한다")
        void returnsFalseWhenPending() {
            // given
            User user = User.builder()
                    .status(UserStatus.PENDING)
                    .build();

            // when & then
            assertThat(user.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isMaster() - 마스터 권한 확인")
    class IsMaster {

        @Test
        @DisplayName("MASTER 역할이면 true를 반환한다")
        void returnsTrueWhenMaster() {
            // given
            User user = User.builder()
                    .role(UserRole.MASTER)
                    .build();

            // when & then
            assertThat(user.isMaster()).isTrue();
        }

        @Test
        @DisplayName("USER 역할이면 false를 반환한다")
        void returnsFalseWhenUser() {
            // given
            User user = User.builder()
                    .role(UserRole.USER)
                    .build();

            // when & then
            assertThat(user.isMaster()).isFalse();
        }
    }

    @Nested
    @DisplayName("approve() - 사용자 승인")
    class Approve {

        @Test
        @DisplayName("PENDING 상태를 ACTIVE로 변경한다")
        void changesPendingToActive() {
            // given
            User user = User.builder()
                    .status(UserStatus.PENDING)
                    .build();

            // when
            user.approve();

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("이미 ACTIVE인 경우에도 ACTIVE 상태를 유지한다")
        void keepsActiveWhenAlreadyActive() {
            // given
            User user = User.builder()
                    .status(UserStatus.ACTIVE)
                    .build();

            // when
            user.approve();

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("updateLastLogin() - 마지막 로그인 시간 업데이트")
    class UpdateLastLogin {

        @Test
        @DisplayName("마지막 로그인 시간을 현재 시간으로 업데이트한다")
        void updatesLastLoginToNow() {
            // given
            User user = User.builder()
                    .lastLoginAt(LocalDateTime.now().minusDays(7))
                    .build();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // when
            user.updateLastLogin();

            // then
            assertThat(user.getLastLoginAt()).isAfter(before);
            assertThat(user.getLastLoginAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        }

        @Test
        @DisplayName("마지막 로그인이 null인 경우에도 업데이트된다")
        void updatesEvenWhenNull() {
            // given
            User user = User.builder()
                    .lastLoginAt(null)
                    .build();

            // when
            user.updateLastLogin();

            // then
            assertThat(user.getLastLoginAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder - 사용자 생성")
    class Builder {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void setsAllFieldsCorrectly() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // when
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .email("test@example.com")
                    .name("테스트사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .lastLoginAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // then
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getNaverId()).isEqualTo("naver-123");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getName()).isEqualTo("테스트사용자");
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getLastLoginAt()).isEqualTo(now);
            assertThat(user.getCreatedAt()).isEqualTo(now);
            assertThat(user.getUpdatedAt()).isEqualTo(now);
        }
    }
}

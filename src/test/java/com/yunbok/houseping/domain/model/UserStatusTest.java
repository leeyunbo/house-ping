package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserStatus - 사용자 상태 열거형")
class UserStatusTest {

    @Test
    @DisplayName("ACTIVE 상태가 존재한다")
    void activeStatusExists() {
        assertThat(UserStatus.ACTIVE).isNotNull();
        assertThat(UserStatus.ACTIVE.name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("PENDING 상태가 존재한다")
    void pendingStatusExists() {
        assertThat(UserStatus.PENDING).isNotNull();
        assertThat(UserStatus.PENDING.name()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("상태는 2개만 존재한다")
    void onlyTwoStatusesExist() {
        assertThat(UserStatus.values()).hasSize(2);
    }
}

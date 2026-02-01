package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRole - 사용자 역할 열거형")
class UserRoleTest {

    @Test
    @DisplayName("MASTER 역할이 존재한다")
    void masterRoleExists() {
        assertThat(UserRole.MASTER).isNotNull();
        assertThat(UserRole.MASTER.name()).isEqualTo("MASTER");
    }

    @Test
    @DisplayName("ADMIN 역할이 존재한다")
    void adminRoleExists() {
        assertThat(UserRole.ADMIN).isNotNull();
        assertThat(UserRole.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("USER 역할이 존재한다")
    void userRoleExists() {
        assertThat(UserRole.USER).isNotNull();
        assertThat(UserRole.USER.name()).isEqualTo("USER");
    }

    @Test
    @DisplayName("역할은 3개만 존재한다")
    void onlyThreeRolesExist() {
        assertThat(UserRole.values()).hasSize(3);
    }
}

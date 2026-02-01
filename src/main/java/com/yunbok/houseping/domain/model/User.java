package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String naverId;
    private String email;
    private String name;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isMaster() {
        return role == UserRole.MASTER;
    }

    public void approve() {
        this.status = UserStatus.ACTIVE;
    }

    public void promoteToAdmin() {
        if (this.role == UserRole.MASTER) {
            throw new IllegalStateException("MASTER는 역할을 변경할 수 없습니다.");
        }
        this.role = UserRole.ADMIN;
    }

    public void demoteToUser() {
        if (this.role == UserRole.MASTER) {
            throw new IllegalStateException("MASTER는 역할을 변경할 수 없습니다.");
        }
        this.role = UserRole.USER;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}

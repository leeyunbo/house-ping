package com.yunbok.houseping.entity;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.domain.UserRole;
import com.yunbok.houseping.core.domain.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(name = "uk_naver_id", columnNames = "naver_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "naver_id", nullable = false, unique = true)
    private String naverId;

    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static UserEntity from(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .naverId(user.getNaverId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toDomain() {
        return User.builder()
                .id(id)
                .naverId(naverId)
                .email(email)
                .name(name)
                .role(role)
                .status(status)
                .lastLoginAt(lastLoginAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}

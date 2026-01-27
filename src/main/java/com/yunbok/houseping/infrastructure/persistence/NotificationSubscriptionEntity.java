package com.yunbok.houseping.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 구독 엔티티
 * 사용자가 특정 청약에 대해 알림을 받기로 설정한 정보
 */
@Entity
@Table(name = "notification_subscription",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_subscription",
                columnNames = {"subscription_id"}
        ),
        indexes = {
                @Index(name = "idx_notification_enabled", columnList = "enabled"),
                @Index(name = "idx_notification_dates", columnList = "receipt_start_notified, receipt_end_notified")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 청약 정보 ID
     */
    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    /**
     * 알림 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * 접수 시작 1일 전 알림 발송 여부
     */
    @Column(name = "receipt_start_notified", nullable = false)
    @Builder.Default
    private boolean receiptStartNotified = false;

    /**
     * 접수 종료 당일 알림 발송 여부
     */
    @Column(name = "receipt_end_notified", nullable = false)
    @Builder.Default
    private boolean receiptEndNotified = false;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void markReceiptStartNotified() {
        this.receiptStartNotified = true;
    }

    public void markReceiptEndNotified() {
        this.receiptEndNotified = true;
    }

    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }

    public void disable() {
        this.enabled = false;
    }
}

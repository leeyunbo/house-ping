package com.yunbok.houseping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 발송 이력 엔티티
 */
@Entity
@Table(name = "notification_history",
        indexes = {
                @Index(name = "idx_notification_history_sent_at", columnList = "sent_at DESC"),
                @Index(name = "idx_notification_history_type", columnList = "notification_type")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 유형 (DAILY_REPORT, RECEIPT_START, RECEIPT_END, NEW_SUBSCRIPTION, ERROR)
     */
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    /**
     * 발송 채널 (SLACK, TELEGRAM, ALL)
     */
    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    /**
     * 발송 성공 여부
     */
    @Column(nullable = false)
    private boolean success;

    /**
     * 발송 내용 요약
     */
    @Column(name = "summary", length = 500)
    private String summary;

    /**
     * 상세 내용 (JSON 형태로 저장 가능)
     */
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    /**
     * 에러 메시지 (실패 시)
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 발송 일시
     */
    @CreatedDate
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    /**
     * 발송자 (SCHEDULER, MANUAL)
     */
    @Column(name = "triggered_by", length = 50)
    private String triggeredBy;
}

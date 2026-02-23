package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscriptionEntity, Long> {

    Optional<NotificationSubscriptionEntity> findBySubscriptionId(Long subscriptionId);

    boolean existsBySubscriptionId(Long subscriptionId);

    void deleteBySubscriptionId(Long subscriptionId);

    /**
     * 접수 시작 알림 대상 조회 (활성화 + 미발송)
     */
    @Query("SELECT n FROM NotificationSubscriptionEntity n WHERE n.enabled = true AND n.receiptStartNotified = false")
    List<NotificationSubscriptionEntity> findPendingReceiptStartNotifications();

    /**
     * 접수 종료 알림 대상 조회 (활성화 + 미발송)
     */
    @Query("SELECT n FROM NotificationSubscriptionEntity n WHERE n.enabled = true AND n.receiptEndNotified = false")
    List<NotificationSubscriptionEntity> findPendingReceiptEndNotifications();

    /**
     * 활성화된 알림 구독 목록
     */
    List<NotificationSubscriptionEntity> findByEnabledTrue();

    /**
     * 특정 청약 ID 목록에 대한 구독 조회
     */
    @Query("SELECT n FROM NotificationSubscriptionEntity n WHERE n.subscriptionId IN :subscriptionIds AND n.enabled = true")
    List<NotificationSubscriptionEntity> findBySubscriptionIdInAndEnabledTrue(@Param("subscriptionIds") List<Long> subscriptionIds);
}

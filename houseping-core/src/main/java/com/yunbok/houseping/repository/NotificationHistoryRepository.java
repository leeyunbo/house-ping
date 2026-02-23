package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 발송 이력 레포지토리
 */
@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistoryEntity, Long> {

    /**
     * 최근 발송 이력 조회 (최신순)
     */
    List<NotificationHistoryEntity> findTop50ByOrderBySentAtDesc();

    /**
     * 특정 타입의 발송 이력 조회
     */
    List<NotificationHistoryEntity> findByNotificationTypeOrderBySentAtDesc(String notificationType);

    /**
     * 특정 기간 내 발송 이력 조회
     */
    @Query("SELECT h FROM NotificationHistoryEntity h WHERE h.sentAt >= :from AND h.sentAt <= :to ORDER BY h.sentAt DESC")
    List<NotificationHistoryEntity> findByPeriod(LocalDateTime from, LocalDateTime to);

    /**
     * 오늘 발송된 이력 조회
     */
    @Query("SELECT h FROM NotificationHistoryEntity h WHERE h.sentAt >= :startOfDay AND h.sentAt < :endOfDay ORDER BY h.sentAt DESC")
    List<NotificationHistoryEntity> findTodayHistory(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * 실패한 발송 이력 조회
     */
    List<NotificationHistoryEntity> findBySuccessFalseOrderBySentAtDesc();

    /**
     * 페이지네이션을 위한 조회
     */
    Page<NotificationHistoryEntity> findAllByOrderBySentAtDesc(Pageable pageable);
}

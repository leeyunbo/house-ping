package com.yunbok.houseping.infrastructure.persistence.repository;

import com.yunbok.houseping.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 청약 정보 Repository
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    /**
     * 특정 날짜에 접수 시작하는 청약 조회
     */
    List<SubscriptionEntity> findByReceiptStartDate(LocalDate receiptStartDate);

    /**
     * 특정 지역의 모든 청약 조회
     */
    List<SubscriptionEntity> findByArea(String area);

    /**
     * 특정 지역 + 접수 시작일로 조회
     */
    List<SubscriptionEntity> findByAreaAndReceiptStartDate(String area, LocalDate receiptStartDate);

    /**
     * 특정 지역 + 금일 이후 접수 시작 청약 조회
     */
    List<SubscriptionEntity> findByAreaAndReceiptStartDateGreaterThanEqual(String area, LocalDate fromDate);

    /**
     * 중복 체크용: source + houseName + receiptStartDate로 조회
     */
    Optional<SubscriptionEntity> findBySourceAndHouseNameAndReceiptStartDate(
            String source,
            String houseName,
            LocalDate receiptStartDate
    );

    /**
     * 특정 기간 내 청약 조회
     */
    @Query("SELECT s FROM SubscriptionEntity s " +
           "WHERE s.receiptStartDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.receiptStartDate ASC")
    List<SubscriptionEntity> findByReceiptStartDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 소스의 데이터 조회
     */
    List<SubscriptionEntity> findBySource(String source);

    /**
     * 오래된 데이터 삭제 (1년 이상 지난 데이터)
     */
    @Modifying
    @Query("DELETE FROM SubscriptionEntity s WHERE s.receiptStartDate < :cutoffDate")
    int deleteOldSubscriptions(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * 마지막 수집 시간 조회
     */
    @Query("SELECT MAX(s.collectedAt) FROM SubscriptionEntity s WHERE s.source = :source")
    Optional<LocalDateTime> findLastCollectedTimeBySource(@Param("source") String source);

    /**
     * 데이터 건수 조회
     */
    long countBySource(String source);
}

package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import com.yunbok.houseping.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
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
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long>,
        QuerydslPredicateExecutor<SubscriptionEntity> {

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

    /**
     * 사용 가능한 지역 목록
     */
    @Query("SELECT DISTINCT s.area FROM SubscriptionEntity s WHERE s.area IS NOT NULL ORDER BY s.area")
    List<String> findDistinctAreas();

    /**
     * 데이터 소스 목록
     */
    @Query("SELECT DISTINCT s.source FROM SubscriptionEntity s WHERE s.source IS NOT NULL ORDER BY s.source")
    List<String> findDistinctSources();

    /**
     * 주택 유형 목록
     */
    @Query("SELECT DISTINCT s.houseType FROM SubscriptionEntity s WHERE s.houseType IS NOT NULL ORDER BY s.houseType")
    List<String> findDistinctHouseTypes();

    /**
     * 특정 지역들의 house_manage_no 목록 조회
     */
    @Query("SELECT DISTINCT s.houseManageNo FROM SubscriptionEntity s WHERE s.area IN :areas AND s.houseManageNo IS NOT NULL")
    List<String> findHouseManageNosByAreaIn(@Param("areas") List<String> areas);

    /**
     * 지역명 부분 일치로 청약 조회 (공개 페이지용)
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.area LIKE %:area% ORDER BY s.receiptStartDate DESC")
    List<SubscriptionEntity> findByAreaContaining(@Param("area") String area);

    /**
     * 소스와 지역 목록으로 청약 조회
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.source = :source AND s.area IN :areas ORDER BY s.receiptStartDate DESC")
    List<SubscriptionEntity> findBySourceAndAreaIn(@Param("source") String source, @Param("areas") List<String> areas);

    /**
     * 지역 목록으로 청약 조회 (모든 소스)
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.area LIKE %:area1% OR s.area LIKE %:area2% ORDER BY s.receiptStartDate DESC")
    List<SubscriptionEntity> findByAreaLikeOrAreaLike(@Param("area1") String area1, @Param("area2") String area2);

    /**
     * 접수 기간이 주어진 기간과 겹치는 청약 조회
     */
    @Query("SELECT s FROM SubscriptionEntity s " +
           "WHERE s.receiptStartDate <= :weekEnd " +
           "AND (s.receiptEndDate >= :weekStart OR s.receiptEndDate IS NULL) " +
           "ORDER BY s.receiptStartDate ASC")
    List<SubscriptionEntity> findByReceiptPeriodOverlapping(
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);
}

package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RealTransactionCacheRepository extends JpaRepository<RealTransactionCacheEntity, Long> {

    /**
     * 법정동코드와 계약월로 조회
     */
    List<RealTransactionCacheEntity> findByLawdCdAndDealYmd(String lawdCd, String dealYmd);

    /**
     * 법정동코드로 최근 거래 조회 (금액 내림차순)
     */
    @Query("SELECT r FROM RealTransactionCacheEntity r WHERE r.lawdCd = :lawdCd ORDER BY r.dealDate DESC, r.dealAmount DESC")
    List<RealTransactionCacheEntity> findRecentByLawdCd(@Param("lawdCd") String lawdCd);

    /**
     * 법정동코드와 아파트명으로 조회
     */
    @Query("SELECT r FROM RealTransactionCacheEntity r WHERE r.lawdCd = :lawdCd AND r.aptName LIKE %:aptName% ORDER BY r.dealDate DESC")
    List<RealTransactionCacheEntity> findByLawdCdAndAptNameContaining(
            @Param("lawdCd") String lawdCd,
            @Param("aptName") String aptName);

    /**
     * 캐시 유효성 확인 (해당 법정동코드 + 계약월 데이터가 있는지)
     */
    boolean existsByLawdCdAndDealYmd(String lawdCd, String dealYmd);

    /**
     * 오래된 캐시 삭제 (기본 7일)
     */
    @Modifying
    @Query("DELETE FROM RealTransactionCacheEntity r WHERE r.cachedAt < :threshold")
    int deleteOldCache(@Param("threshold") LocalDateTime threshold);

    /**
     * 법정동코드와 전용면적 범위로 평균 거래가 조회
     */
    @Query("SELECT AVG(r.dealAmount) FROM RealTransactionCacheEntity r " +
           "WHERE r.lawdCd = :lawdCd " +
           "AND r.excluUseAr BETWEEN :minArea AND :maxArea " +
           "AND r.dealYmd >= :fromYmd")
    Long findAverageAmountByLawdCdAndAreaRange(
            @Param("lawdCd") String lawdCd,
            @Param("minArea") java.math.BigDecimal minArea,
            @Param("maxArea") java.math.BigDecimal maxArea,
            @Param("fromYmd") String fromYmd);

    /**
     * 법정동코드와 동명으로 최근 거래 조회
     */
    @Query("SELECT r FROM RealTransactionCacheEntity r WHERE r.lawdCd = :lawdCd AND r.umdNm = :umdNm ORDER BY r.dealDate DESC")
    List<RealTransactionCacheEntity> findByLawdCdAndUmdNmOrderByDealDateDesc(
            @Param("lawdCd") String lawdCd,
            @Param("umdNm") String umdNm);

    /**
     * 법정동코드로 최근 거래 조회 (날짜 내림차순)
     */
    @Query("SELECT r FROM RealTransactionCacheEntity r WHERE r.lawdCd = :lawdCd ORDER BY r.dealDate DESC")
    List<RealTransactionCacheEntity> findByLawdCdOrderByDealDateDesc(@Param("lawdCd") String lawdCd);
}

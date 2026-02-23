package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 청약 분양가 상세 정보 Repository
 */
public interface SubscriptionPriceRepository extends JpaRepository<SubscriptionPriceEntity, Long> {

    /**
     * 주택관리번호로 분양가 목록 조회
     */
    List<SubscriptionPriceEntity> findByHouseManageNo(String houseManageNo);

    /**
     * 주택관리번호와 공고번호로 분양가 목록 조회
     */
    List<SubscriptionPriceEntity> findByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    /**
     * 특정 모델 분양가 조회
     */
    Optional<SubscriptionPriceEntity> findByHouseManageNoAndPblancNoAndModelNo(
            String houseManageNo, String pblancNo, String modelNo);

    /**
     * 주택관리번호로 최고 분양가 조회
     */
    @Query("SELECT MAX(p.topAmount) FROM SubscriptionPriceEntity p WHERE p.houseManageNo = :houseManageNo")
    Optional<Long> findMaxTopAmountByHouseManageNo(@Param("houseManageNo") String houseManageNo);

    /**
     * 주택관리번호로 최저 분양가 조회
     */
    @Query("SELECT MIN(p.topAmount) FROM SubscriptionPriceEntity p WHERE p.houseManageNo = :houseManageNo AND p.topAmount > 0")
    Optional<Long> findMinTopAmountByHouseManageNo(@Param("houseManageNo") String houseManageNo);

    /**
     * 주택관리번호로 평균 평당가 조회
     */
    @Query("SELECT AVG(p.pricePerPyeong) FROM SubscriptionPriceEntity p WHERE p.houseManageNo = :houseManageNo AND p.pricePerPyeong > 0")
    Optional<Double> findAvgPricePerPyeongByHouseManageNo(@Param("houseManageNo") String houseManageNo);

    /**
     * 분양가 데이터 존재 여부 확인
     */
    boolean existsByHouseManageNo(String houseManageNo);
}

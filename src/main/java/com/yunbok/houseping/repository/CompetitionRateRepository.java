package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 경쟁률 Repository
 */
@Repository
public interface CompetitionRateRepository extends JpaRepository<CompetitionRateEntity, Long>,
        QuerydslPredicateExecutor<CompetitionRateEntity> {

    /**
     * 특정 청약 건의 경쟁률 조회
     */
    List<CompetitionRateEntity> findByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    /**
     * 특정 청약 건의 경쟁률 존재 여부 확인
     */
    boolean existsByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    /**
     * 고유한 주택형 목록
     */
    @Query("SELECT DISTINCT c.houseType FROM CompetitionRateEntity c WHERE c.houseType IS NOT NULL ORDER BY c.houseType")
    List<String> findDistinctHouseTypes();
}

package com.yunbok.houseping.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionCodeRepository extends JpaRepository<RegionCodeEntity, Long> {

    /**
     * 법정동코드로 조회
     */
    Optional<RegionCodeEntity> findByLawdCd(String lawdCd);

    /**
     * 시군구명으로 조회 (부분 일치)
     */
    @Query("SELECT r FROM RegionCodeEntity r WHERE r.sigunguName LIKE %:sigunguName%")
    List<RegionCodeEntity> findBySigunguNameContaining(@Param("sigunguName") String sigunguName);

    /**
     * 전체 지역명으로 조회 (부분 일치)
     */
    @Query("SELECT r FROM RegionCodeEntity r WHERE r.regionName LIKE %:regionName%")
    List<RegionCodeEntity> findByRegionNameContaining(@Param("regionName") String regionName);

    /**
     * 시도명으로 목록 조회
     */
    List<RegionCodeEntity> findBySidoName(String sidoName);

    /**
     * 시도명과 시군구명으로 조회
     */
    Optional<RegionCodeEntity> findBySidoNameAndSigunguName(String sidoName, String sigunguName);
}

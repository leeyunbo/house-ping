package com.yunbok.houseping.domain.port.out;

import java.util.Optional;

/**
 * 지역코드 조회 Port
 */
public interface RegionCodeQueryPort {

    /**
     * 시도명과 시군구명으로 법정동코드 조회
     */
    Optional<String> findLawdCd(String sidoName, String sigunguName);

    /**
     * 시군구명 부분 일치로 법정동코드 조회
     */
    Optional<String> findLawdCdByContaining(String sigunguName);
}

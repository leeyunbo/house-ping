package com.yunbok.houseping.core.port;

import java.util.Map;
import java.util.Optional;

public interface ApartmentLocationPort {

    /**
     * 아파트 좌표 조회
     */
    Optional<double[]> findLocation(String aptName, String lawdCd);

    /**
     * 법정동코드로 해당 지역 아파트 좌표 전체 조회
     * @return aptName → [latitude, longitude]
     */
    Map<String, double[]> findLocationsByLawdCd(String lawdCd);

    /**
     * 좌표 존재 여부
     */
    boolean exists(String aptName, String lawdCd);

    /**
     * 좌표 저장
     */
    void save(String aptName, String lawdCd, String dongName, String jibun, double latitude, double longitude);
}

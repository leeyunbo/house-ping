package com.yunbok.houseping.core.port;

/**
 * 아파트 좌표 조회 Port (좌표가 없으면 외부 API로 조회 후 저장)
 */
public interface ApartmentGeocodingPort {

    void geocodeIfAbsent(String aptName, String lawdCd, String dongName, String jibun);
}

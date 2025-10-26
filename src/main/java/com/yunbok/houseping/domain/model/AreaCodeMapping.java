package com.yunbok.houseping.domain.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * 청약Home 지역명과 LH 지역코드 매핑 Enum
 */
@Getter
public enum AreaCodeMapping {

    SEOUL("서울", "11", "100"),
    GYEONGGI("경기", "41", "410"),
    INCHEON("인천", "28", "400"),
    BUSAN("부산", "26", "600"),
    DAEGU("대구", "27", "700"),
    DAEJEON("대전", "30", "300"),
    GWANGJU("광주", "29", "500"),
    ULSAN("울산", "31", "680"),
    SEJONG("세종", "36110", "338"),
    GANGWON("강원", "42", "200"),
    CHUNGBUK("충북", "43", "360"),
    CHUNGNAM("충남", "44", "312"),
    JEONBUK("전북", "52", "560"),
    JEONNAM("전남", "46", "513"),
    GYEONGBUK("경북", "47", "712"),
    GYEONGNAM("경남", "48", "621"),
    JEJU("제주", "50", "690");

    private final String areaName;        // 지역명 (한국어)
    private final String lhAreaCode;      // LH API 지역코드
    private final String applyHomeAreaCode; // 청약Home API 지역코드

    AreaCodeMapping(String areaName, String lhAreaCode, String applyHomeAreaCode) {
        this.areaName = areaName;
        this.lhAreaCode = lhAreaCode;
        this.applyHomeAreaCode = applyHomeAreaCode;
    }

    /**
     * 지역명으로 LH 지역코드 조회
     */
    public static String getLhAreaCodeByName(String areaName) {
        return Arrays.stream(values())
                .filter(area -> area.areaName.equals(areaName))
                .map(AreaCodeMapping::getLhAreaCode)
                .findFirst()
                .orElse(areaName); // 매핑되지 않은 경우 원본 반환
    }

    /**
     * 청약Home 지역코드로 지역명 조회
     */
    public static String getAreaNameByApplyHomeCode(String applyHomeCode) {
        return Arrays.stream(values())
                .filter(area -> area.applyHomeAreaCode.equals(applyHomeCode))
                .map(AreaCodeMapping::getAreaName)
                .findFirst()
                .orElse("알 수 없는 지역");
    }

    /**
     * 지역명으로 청약Home 지역코드 조회
     */
    public static String getApplyHomeCodeByName(String areaName) {
        return Arrays.stream(values())
                .filter(area -> area.areaName.equals(areaName))
                .map(AreaCodeMapping::getApplyHomeAreaCode)
                .findFirst()
                .orElse(areaName);
    }
}
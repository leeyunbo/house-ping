package com.yunbok.houseping.domain.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * 청약Home 주택 구분 코드와 명칭 매핑 Enum
 */
@Getter
public enum HouseType {

    APT("01", "APT"),
    PRIVATE_PRE_SUBSCRIPTION("09", "민간사전청약"),
    NEWLYWED_TOWN("10", "신혼희망타운"),
    REMAINING("", "무순위"),
    OTHER("", "기타");

    private final String houseSecd;    // 청약Home API 주택구분코드
    private final String displayName;  // 표시명

    HouseType(String houseSecd, String displayName) {
        this.houseSecd = houseSecd;
        this.displayName = displayName;
    }

    /**
     * 주택구분코드로 표시명 조회
     */
    public static String getDisplayNameByCode(String houseSecd) {
        return Arrays.stream(values())
                .filter(type -> type.houseSecd.equals(houseSecd))
                .map(HouseType::getDisplayName)
                .findFirst()
                .orElse(OTHER.displayName);
    }

    /**
     * 표시명으로 주택구분코드 조회
     */
    public static String getCodeByDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equals(displayName))
                .map(HouseType::getHouseSecd)
                .findFirst()
                .orElse(OTHER.houseSecd);
    }
}
package com.yunbok.houseping.core.domain;

import lombok.Getter;

import java.util.Arrays;

/**
 * 청약Home 주택 구분 코드와 명칭 매핑 Enum
 */
@Getter
public enum HouseType {

    APT("01", "APT", "/getAPTLttotPblancDetail", "/getAPTLttotPblancMdl"),
    PRIVATE_PRE_SUBSCRIPTION("09", "민간사전청약", "/getAPTLttotPblancDetail", "/getAPTLttotPblancMdl"),
    NEWLYWED_TOWN("10", "신혼희망타운", "/getAPTLttotPblancDetail", "/getAPTLttotPblancMdl"),
    REMAINING("", "무순위", "/getRemndrLttotPblancDetail", "/getRemndrLttotPblancMdl"),
    ARBITRARY("", "임의공급", "/getOPTLttotPblancDetail", "/getOPTLttotPblancMdl"),
    OTHER("", "기타", "", "");

    private final String houseSecd;    // 청약Home API 주택구분코드
    private final String displayName;  // 표시명
    private final String detailPath;   // 청약 상세 API 경로
    private final String pricePath;    // 분양가 API 경로

    HouseType(String houseSecd, String displayName, String detailPath, String pricePath) {
        this.houseSecd = houseSecd;
        this.displayName = displayName;
        this.detailPath = detailPath;
        this.pricePath = pricePath;
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

    /**
     * 표시명으로 HouseType 조회
     */
    public static HouseType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equals(displayName))
                .findFirst()
                .orElse(OTHER);
    }

    /**
     * APT 계열 API를 사용하는 타입인지 (houseSecd 파라미터 필요)
     */
    public boolean usesHouseSecd() {
        return !houseSecd.isEmpty();
    }
}

package com.yunbok.houseping.domain.model;

import lombok.Getter;

/**
 * LH API 공고유형코드 Enum
 */
@Getter
public enum LhApiTypeCode {

    SALE_APT("05", "LH 분양주택"),
    NEWLYWED_APT("39", "LH 신혼희망타운"),
    RENTAL_APT("06", "LH 임대주택");

    private final String typeCode;    // LH API 공고유형코드 (UPP_AIS_TP_CD)
    private final String displayName; // 표시명

    LhApiTypeCode(String typeCode, String displayName) {
        this.typeCode = typeCode;
        this.displayName = displayName;
    }
}
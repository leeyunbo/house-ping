package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 아파트 실거래 상세 항목
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTransactionItem {

    /**
     * 아파트명
     */
    @JacksonXmlProperty(localName = "aptNm")
    private String aptName;

    /**
     * 거래금액 (만원, 콤마 포함 문자열)
     */
    @JacksonXmlProperty(localName = "dealAmount")
    private String dealAmount;

    /**
     * 거래년도
     */
    @JacksonXmlProperty(localName = "dealYear")
    private Integer dealYear;

    /**
     * 거래월
     */
    @JacksonXmlProperty(localName = "dealMonth")
    private Integer dealMonth;

    /**
     * 거래일
     */
    @JacksonXmlProperty(localName = "dealDay")
    private Integer dealDay;

    /**
     * 전용면적 (㎡)
     */
    @JacksonXmlProperty(localName = "excluUseAr")
    private BigDecimal excluUseAr;

    /**
     * 층
     */
    @JacksonXmlProperty(localName = "floor")
    private Integer floor;

    /**
     * 건축년도
     */
    @JacksonXmlProperty(localName = "buildYear")
    private Integer buildYear;

    /**
     * 법정동
     */
    @JacksonXmlProperty(localName = "umdNm")
    private String umdNm;

    /**
     * 지번
     */
    @JacksonXmlProperty(localName = "jibun")
    private String jibun;

    /**
     * 거래금액을 Long으로 파싱 (콤마 제거)
     */
    public Long getDealAmountAsLong() {
        if (dealAmount == null || dealAmount.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(dealAmount.replaceAll("[,\\s]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

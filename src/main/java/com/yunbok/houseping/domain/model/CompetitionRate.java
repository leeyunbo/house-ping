package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 청약 경쟁률 정보
 * 당첨자 발표일 이후 API에서 조회 가능
 */
@Getter
@Builder
public class CompetitionRate {

    /**
     * 주택관리번호
     */
    private String houseManageNo;

    /**
     * 공고번호
     */
    private String pblancNo;

    /**
     * 주택형 (084.9543T)
     */
    private String houseType;

    /**
     * 공급세대수
     */
    private Integer supplyCount;

    /**
     * 접수건수
     */
    private Integer requestCount;

    /**
     * 경쟁률
     */
    private BigDecimal competitionRate;

    /**
     * 거주지역 (해당지역/기타지역)
     */
    private String residenceArea;

    /**
     * 순위 (1순위/2순위)
     */
    private Integer rank;
}

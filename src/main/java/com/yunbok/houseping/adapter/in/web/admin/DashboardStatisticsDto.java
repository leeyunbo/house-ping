package com.yunbok.houseping.adapter.in.web.admin;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatisticsDto(
        // 전체 요약
        Summary summary,
        // 지역별 연도별 추이
        AreaYearlyTrend areaYearlyTrend,
        // 거주지역별 통계
        List<ResidenceAreaStat> byResidenceArea,
        // 평수별 통계 (상위 15개)
        List<HouseTypeStat> byHouseType,
        // 경쟁률 분포
        RateDistribution distribution
) {
    public record Summary(
            long totalCount,
            BigDecimal avgRate,
            BigDecimal maxRate,
            BigDecimal minRate,
            int areaCount,
            int houseTypeCount
    ) {}

    public record AreaYearlyTrend(
            List<Integer> years,
            List<AreaYearlyData> data
    ) {}

    public record AreaYearlyData(
            String area,
            List<BigDecimal> rates  // years 순서에 맞는 경쟁률 (없으면 null)
    ) {}

    public record ResidenceAreaStat(
            String residenceArea,
            long count,
            BigDecimal avgRate
    ) {}

    public record HouseTypeStat(
            String houseType,
            String sizeCategory,
            long count,
            BigDecimal avgRate
    ) {}

    public record RateDistribution(
            long under5,      // 5:1 이하
            long from5to10,   // 5~10:1
            long from10to20,  // 10~20:1
            long from20to50,  // 20~50:1
            long over50       // 50:1 이상
    ) {}
}

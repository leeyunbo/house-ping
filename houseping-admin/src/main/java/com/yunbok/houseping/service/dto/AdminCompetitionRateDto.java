package com.yunbok.houseping.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminCompetitionRateDto(
        Long id,
        String houseManageNo,
        String pblancNo,
        String houseType,
        Integer supplyCount,
        Integer requestCount,
        BigDecimal competitionRate,
        String residenceArea,
        Integer rank,
        LocalDateTime collectedAt,
        // 조인된 청약 정보
        String houseName,
        String area
) {
}

package com.yunbok.houseping.persistence;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.port.RealTransactionPersistencePort;
import com.yunbok.houseping.entity.RealTransactionCacheEntity;
import com.yunbok.houseping.repository.RealTransactionCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 실거래가 조회 어댑터
 */
@Component
@RequiredArgsConstructor
public class RealTransactionStore implements RealTransactionPersistencePort {

    private final RealTransactionCacheRepository realTransactionCacheRepository;

    public List<RealTransaction> findByLawdCdAndDongName(String lawdCd, String dongName) {
        return realTransactionCacheRepository.findByLawdCdAndUmdNmOrderByDealDateDesc(lawdCd, dongName).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<RealTransaction> findByLawdCd(String lawdCd) {
        return realTransactionCacheRepository.findByLawdCdOrderByDealDateDesc(lawdCd).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<RealTransaction> findByLawdCdAndDongPrefixAndAreaRange(
            String lawdCd,
            String dongNamePrefix,
            int buildYearMin,
            BigDecimal minArea,
            BigDecimal maxArea) {
        return realTransactionCacheRepository
                .findByLawdCdAndDongPrefixAndAreaRange(lawdCd, dongNamePrefix, buildYearMin, minArea, maxArea)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public boolean hasCachedData(String lawdCd) {
        // 최근 12개월 중 하나라도 캐시가 있는지 확인
        LocalDate now = LocalDate.now().minusMonths(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        for (int i = 0; i < 12; i++) {
            String dealYmd = now.minusMonths(i).format(formatter);
            if (realTransactionCacheRepository.existsByLawdCdAndDealYmd(lawdCd, dealYmd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Entity -> Domain Model 변환
     */
    private RealTransaction toDomain(RealTransactionCacheEntity entity) {
        return RealTransaction.builder()
                .id(entity.getId())
                .lawdCd(entity.getLawdCd())
                .dealYmd(entity.getDealYmd())
                .aptName(entity.getAptName())
                .dealAmount(entity.getDealAmount())
                .exclusiveArea(entity.getExcluUseAr())
                .floor(entity.getFloor())
                .buildYear(entity.getBuildYear())
                .dealDate(entity.getDealDate())
                .dongName(entity.getUmdNm())
                .jibun(entity.getJibun())
                .build();
    }
}

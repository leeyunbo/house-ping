package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.domain.model.RealTransaction;
import com.yunbok.houseping.domain.port.out.RealTransactionQueryPort;
import com.yunbok.houseping.infrastructure.persistence.RealTransactionCacheEntity;
import com.yunbok.houseping.infrastructure.persistence.RealTransactionCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 실거래가 조회 어댑터
 */
@Component
@RequiredArgsConstructor
public class RealTransactionQueryAdapter implements RealTransactionQueryPort {

    private final RealTransactionCacheRepository realTransactionCacheRepository;

    @Override
    public List<RealTransaction> findByLawdCdAndDongName(String lawdCd, String dongName) {
        return realTransactionCacheRepository.findByLawdCdAndUmdNmOrderByDealDateDesc(lawdCd, dongName).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<RealTransaction> findByLawdCd(String lawdCd) {
        return realTransactionCacheRepository.findByLawdCdOrderByDealDateDesc(lawdCd).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean hasCachedData(String lawdCd) {
        // 최근 6개월 중 하나라도 캐시가 있는지 확인
        LocalDate now = LocalDate.now().minusMonths(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        for (int i = 0; i < 6; i++) {
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

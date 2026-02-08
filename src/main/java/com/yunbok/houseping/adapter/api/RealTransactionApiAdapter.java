package com.yunbok.houseping.adapter.api;

import com.yunbok.houseping.support.external.RealTransactionApiResponse;
import com.yunbok.houseping.support.external.RealTransactionItem;
import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.entity.RealTransactionCacheEntity;
import com.yunbok.houseping.repository.RealTransactionCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 국토교통부 아파트 실거래가 API 어댑터
 */
@Slf4j
@Component
public class RealTransactionApiAdapter implements RealTransactionFetchPort {

    private static final int CACHE_VALIDITY_DAYS = 1;

    @Value("${realtransaction.api.key:}")
    private String apiKey;

    private final WebClient webClient;
    private final RealTransactionCacheRepository cacheRepository;

    public RealTransactionApiAdapter(
            @Qualifier("realTransactionWebClient") WebClient webClient,
            RealTransactionCacheRepository cacheRepository) {
        this.webClient = webClient;
        this.cacheRepository = cacheRepository;
    }

    /**
     * 법정동코드와 계약월로 실거래가 조회
     * 캐시가 있으면 캐시에서, 없으면 API 호출 후 캐싱
     */
    public List<RealTransactionCacheEntity> fetchTransactions(String lawdCd, String dealYmd) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[실거래가 API] API 키가 설정되지 않음");
            return Collections.emptyList();
        }

        // 캐시 확인
        List<RealTransactionCacheEntity> cached = cacheRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd);
        if (!cached.isEmpty()) {
            // 캐시 유효성 확인 (1일)
            LocalDateTime threshold = LocalDateTime.now().minusDays(CACHE_VALIDITY_DAYS);
            if (cached.get(0).getCachedAt().isAfter(threshold)) {
                log.debug("[실거래가 API] 캐시 사용: lawdCd={}, dealYmd={}, count={}", lawdCd, dealYmd, cached.size());
                return cached;
            }
        }

        // API 호출
        return fetchFromApi(lawdCd, dealYmd);
    }

    /**
     * 최근 N개월 실거래가 조회
     * 실거래 데이터는 1-2개월 지연 공개되므로 2개월 전부터 조회
     */
    public List<RealTransactionCacheEntity> fetchRecentTransactions(String lawdCd, int months) {
        LocalDate now = LocalDate.now().minusMonths(2); // 2개월 전부터 시작
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        for (int i = 0; i < months; i++) {
            String dealYmd = now.minusMonths(i).format(formatter);
            fetchTransactions(lawdCd, dealYmd);
        }

        return cacheRepository.findRecentByLawdCd(lawdCd);
    }

    private List<RealTransactionCacheEntity> fetchFromApi(String lawdCd, String dealYmd) {
        log.info("[실거래가 API] API 호출: lawdCd={}, dealYmd={}", lawdCd, dealYmd);

        try {
            RealTransactionApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getRTMSDataSvcAptTradeDev")
                            .queryParam("serviceKey", apiKey)
                            .queryParam("LAWD_CD", lawdCd)
                            .queryParam("DEAL_YMD", dealYmd)
                            .queryParam("numOfRows", 1000)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .bodyToMono(RealTransactionApiResponse.class)
                    .block();

            if (response == null || !response.isSuccess()) {
                log.warn("[실거래가 API] API 응답 실패: lawdCd={}, dealYmd={}", lawdCd, dealYmd);
                return Collections.emptyList();
            }

            List<RealTransactionItem> items = response.getItems();
            if (items.isEmpty()) {
                log.debug("[실거래가 API] 데이터 없음: lawdCd={}, dealYmd={}", lawdCd, dealYmd);
                return Collections.emptyList();
            }

            // 캐시에 저장
            List<RealTransactionCacheEntity> entities = items.stream()
                    .filter(item -> item.getDealAmountAsLong() != null)
                    .map(item -> toEntity(item, lawdCd, dealYmd))
                    .toList();

            cacheRepository.saveAll(entities);
            log.info("[실거래가 API] {}건 캐싱 완료: lawdCd={}, dealYmd={}", entities.size(), lawdCd, dealYmd);

            return entities;

        } catch (Exception e) {
            log.error("[실거래가 API] API 호출 실패: lawdCd={}, dealYmd={}, error={}", lawdCd, dealYmd, e.getMessage());
            return Collections.emptyList();
        }
    }

    private RealTransactionCacheEntity toEntity(RealTransactionItem item, String lawdCd, String dealYmd) {
        LocalDate dealDate = null;
        if (item.getDealYear() != null && item.getDealMonth() != null && item.getDealDay() != null) {
            dealDate = LocalDate.of(item.getDealYear(), item.getDealMonth(), item.getDealDay());
        }

        return RealTransactionCacheEntity.builder()
                .lawdCd(lawdCd)
                .dealYmd(dealYmd)
                .aptName(item.getAptName())
                .dealAmount(item.getDealAmountAsLong())
                .excluUseAr(item.getExcluUseAr())
                .floor(item.getFloor())
                .buildYear(item.getBuildYear())
                .dealDate(dealDate)
                .dealDay(item.getDealDay())
                .umdNm(item.getUmdNm())
                .jibun(item.getJibun())
                .build();
    }

    @Override
    public List<RealTransaction> fetchAndCacheRecentTransactions(String lawdCd, int months) {
        List<RealTransactionCacheEntity> entities = fetchRecentTransactions(lawdCd, months);
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

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

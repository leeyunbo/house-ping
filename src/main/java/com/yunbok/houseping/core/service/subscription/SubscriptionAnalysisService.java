package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.infrastructure.persistence.RealTransactionStore;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionPriceStore;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionStore;
import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import com.yunbok.houseping.support.dto.CompetitionRateDetailRow;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.MarketAnalysis;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.support.util.AddressHelper;
import com.yunbok.houseping.core.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 청약 분석 서비스
 * 각 컴포넌트를 조합하여 분석 결과 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionAnalysisService {

    private final SubscriptionStore subscriptionQueryPort;
    private final SubscriptionPriceStore subscriptionPriceQueryPort;
    private final RealTransactionStore realTransactionQueryPort;
    private final RealTransactionFetchPort realTransactionFetchPort;
    private final CompetitionRateRepository competitionRateRepository;

    private final AddressHelper addressParser;
    private final HouseTypeComparisonBuilder comparisonBuilder;
    private final MarketAnalyzer marketAnalyzer;

    public SubscriptionAnalysisResult analyze(Long subscriptionId) {
        Subscription subscription = subscriptionQueryPort.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("청약 정보를 찾을 수 없습니다: " + subscriptionId));

        // 주소 파싱
        String lawdCd = addressParser.extractLawdCd(subscription.getAddress());
        String dongName = addressParser.extractDongName(subscription.getAddress());
        log.info("주소 분석: {} → lawdCd={}, dong={}", subscription.getAddress(), lawdCd, dongName);

        // 실거래가 조회 및 필터링
        List<RealTransaction> allTransactions = fetchTransactions(lawdCd);
        List<RealTransaction> dongTransactions = addressParser.filterByDongName(allTransactions, dongName);
        log.info("동 필터링: {} → {}건 → {}건", dongName, allTransactions.size(), dongTransactions.size());

        // 신축 필터링 (최근 3년 내 준공)
        int newBuildYearThreshold = LocalDate.now().getYear() - 2;
        List<RealTransaction> newBuildTx = dongTransactions.stream()
                .filter(t -> t.getBuildYear() != null && t.getBuildYear() >= newBuildYearThreshold)
                .toList();
        boolean newBuildBased = !newBuildTx.isEmpty();
        log.info("신축 필터링: {}년 이후 준공 {}건, 기준={}", newBuildYearThreshold, newBuildTx.size(), newBuildBased ? "신축" : "비교 미제공");

        // 분양가 정보 조회
        List<SubscriptionPrice> prices = subscription.getHouseManageNo() != null
                ? subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo())
                : List.of();

        // 분석 수행 (신축 거래 있을 때만 시세 비교 제공)
        List<HouseTypeComparison> comparisons = newBuildBased
                ? comparisonBuilder.build(prices, newBuildTx) : List.of();
        MarketAnalysis marketAnalysis = marketAnalyzer.analyze(dongTransactions);

        // 경쟁률 로드
        List<CompetitionRateDetailRow> competitionRates = loadCompetitionRates(subscription.getHouseManageNo());

        return SubscriptionAnalysisResult.builder()
                .subscription(subscription)
                .prices(prices)
                .dongName(dongName)
                .newBuildBased(newBuildBased)
                .recentTransactions(dongTransactions.stream().limit(10).toList())
                .marketAnalysis(marketAnalysis)
                .houseTypeComparisons(comparisons)
                .competitionRates(competitionRates)
                .build();
    }

    private List<CompetitionRateDetailRow> loadCompetitionRates(String houseManageNo) {
        if (houseManageNo == null || houseManageNo.isBlank()) {
            return List.of();
        }
        List<CompetitionRateEntity> rates = competitionRateRepository.findByHouseManageNo(houseManageNo);
        if (rates.isEmpty()) {
            return List.of();
        }
        return rates.stream()
                .map(r -> CompetitionRateDetailRow.builder()
                        .houseType(r.getHouseType())
                        .residenceArea(r.getResidenceArea())
                        .rank(r.getRank())
                        .supplyCount(r.getSupplyCount())
                        .requestCount(r.getRequestCount())
                        .competitionRate(effectiveRate(r))
                        .build())
                .sorted(Comparator.comparing(CompetitionRateDetailRow::getHouseType, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CompetitionRateDetailRow::getRank, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(d -> "해당지역".equals(d.getResidenceArea()) ? 0 : 1))
                .toList();
    }

    private BigDecimal effectiveRate(CompetitionRateEntity r) {
        if (r.getCompetitionRate() != null) {
            return r.getCompetitionRate();
        }
        if (r.getSupplyCount() != null && r.getSupplyCount() > 0 && r.getRequestCount() != null) {
            return BigDecimal.valueOf(r.getRequestCount())
                    .divide(BigDecimal.valueOf(r.getSupplyCount()), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /**
     * 실거래가 조회 (캐시 우선, 없으면 API 호출)
     */
    private List<RealTransaction> fetchTransactions(String lawdCd) {
        if (lawdCd == null) {
            return Collections.emptyList();
        }

        List<RealTransaction> transactions = realTransactionQueryPort.findByLawdCd(lawdCd);

        if (transactions.isEmpty()) {
            log.info("캐시 없음, API 직접 조회: lawdCd={}", lawdCd);
            transactions = realTransactionFetchPort.fetchAndCacheRecentTransactions(lawdCd, 6);
            log.info("API 조회 완료: {}건", transactions.size());
        }

        return transactions;
    }
}

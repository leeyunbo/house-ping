package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.persistence.RealTransactionQueryAdapter;
import com.yunbok.houseping.adapter.persistence.SubscriptionPriceQueryAdapter;
import com.yunbok.houseping.adapter.persistence.SubscriptionQueryAdapter;
import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.MarketAnalysis;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.support.util.AddressHelper;
import com.yunbok.houseping.core.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 청약 분석 서비스
 * 각 컴포넌트를 조합하여 분석 결과 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionAnalysisService {

    private final SubscriptionQueryAdapter subscriptionQueryPort;
    private final SubscriptionPriceQueryAdapter subscriptionPriceQueryPort;
    private final RealTransactionQueryAdapter realTransactionQueryPort;
    private final RealTransactionFetchPort realTransactionFetchPort;

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

        // 분양가 정보 조회
        List<SubscriptionPrice> prices = subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo());

        // 분석 수행
        List<HouseTypeComparison> comparisons = comparisonBuilder.build(prices, dongTransactions);
        MarketAnalysis marketAnalysis = marketAnalyzer.analyze(dongTransactions);

        return SubscriptionAnalysisResult.builder()
                .subscription(subscription)
                .prices(prices)
                .dongName(dongName)
                .recentTransactions(dongTransactions.stream().limit(10).toList())
                .marketAnalysis(marketAnalysis)
                .houseTypeComparisons(comparisons)
                .build();
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

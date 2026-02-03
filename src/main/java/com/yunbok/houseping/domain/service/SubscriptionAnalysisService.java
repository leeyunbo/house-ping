package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.*;
import com.yunbok.houseping.domain.port.in.SubscriptionAnalysisUseCase;
import com.yunbok.houseping.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 청약 분석 서비스
 * 청약 정보와 실거래가를 결합하여 분석 데이터 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionAnalysisService implements SubscriptionAnalysisUseCase {

    private final SubscriptionQueryPort subscriptionQueryPort;
    private final SubscriptionPriceQueryPort subscriptionPriceQueryPort;
    private final RealTransactionQueryPort realTransactionQueryPort;
    private final RealTransactionFetchPort realTransactionFetchPort;
    private final RegionCodeQueryPort regionCodeQueryPort;

    // 시도 + 시군구 패턴 (용인시 수지구 같은 복합 구조도 지원)
    private static final Pattern SIGUNGU_PATTERN = Pattern.compile("(서울특별시|부산광역시|대구광역시|인천광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|경기도|강원도|충청북도|충청남도|전라북도|전라남도|경상북도|경상남도|제주특별자치도)\\s+([가-힣]+시)\\s+([가-힣]+구)");
    private static final Pattern SIMPLE_SIGUNGU_PATTERN = Pattern.compile("(서울특별시|부산광역시|대구광역시|인천광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|경기도|강원도|충청북도|충청남도|전라북도|전라남도|경상북도|경상남도|제주특별자치도)\\s+([가-힣]+[시군구])");

    // 동 이름 추출 패턴: 괄호 안 또는 일반 주소에서 "XX동" 추출
    private static final Pattern DONG_IN_PAREN_PATTERN = Pattern.compile("\\(.*?([가-힣]+동)\\)");
    private static final Pattern DONG_PATTERN = Pattern.compile("([가-힣]+[0-9]*동)(?:\\s|$)");

    @Override
    public SubscriptionAnalysisResult analyze(Long subscriptionId) {
        Subscription subscription = subscriptionQueryPort.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("청약 정보를 찾을 수 없습니다: " + subscriptionId));

        // 법정동코드 + 동 이름 추출
        String lawdCd = extractLawdCd(subscription.getAddress());
        String dongName = extractDongName(subscription.getAddress());
        log.info("주소 분석: {} → lawdCd={}, dong={}", subscription.getAddress(), lawdCd, dongName);

        // 실거래가 조회 (캐시 우선, 없으면 API 호출)
        List<RealTransaction> allTransactions = fetchTransactions(lawdCd);

        // 동 단위로 필터링 (행정동 숫자 제거해서 법정동과 비교)
        List<RealTransaction> dongTransactions = filterByDongName(allTransactions, dongName);
        log.info("동 필터링: {} → {}건 → {}건", dongName, allTransactions.size(), dongTransactions.size());

        // 분양가 정보 조회
        List<SubscriptionPrice> prices = subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo());

        // 주택형별 시세 비교
        List<HouseTypeComparison> comparisons = buildHouseTypeComparisons(prices, dongTransactions);

        // 시장 분석 (동 단위)
        MarketAnalysis marketAnalysis = analyzeMarket(dongTransactions);

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
     * 실거래가 조회 (캐시 우선, 없으면 API 호출 후 캐싱)
     */
    private List<RealTransaction> fetchTransactions(String lawdCd) {
        if (lawdCd == null) {
            return Collections.emptyList();
        }

        // 캐시에서 조회
        List<RealTransaction> transactions = realTransactionQueryPort.findByLawdCd(lawdCd);

        // 캐시가 비어있으면 API 호출 후 캐싱
        if (transactions.isEmpty()) {
            log.info("캐시 없음, API 직접 조회: lawdCd={}", lawdCd);
            transactions = realTransactionFetchPort.fetchAndCacheRecentTransactions(lawdCd, 6);
            log.info("API 조회 완료: {}건", transactions.size());
        }

        return transactions;
    }

    /**
     * 동 이름으로 필터링 (정규화 적용)
     */
    private List<RealTransaction> filterByDongName(List<RealTransaction> transactions, String dongName) {
        if (dongName == null || transactions.isEmpty()) {
            return transactions;
        }

        String normalizedDongName = normalizeDongName(dongName);
        return transactions.stream()
                .filter(t -> t.getDongName() != null && normalizeDongName(t.getDongName()).equals(normalizedDongName))
                .toList();
    }

    /**
     * 주소에서 동 이름 추출
     */
    private String extractDongName(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 1차: 괄호 안에서 동 찾기 (더 정확함)
        Matcher parenMatcher = DONG_IN_PAREN_PATTERN.matcher(address);
        if (parenMatcher.find()) {
            return parenMatcher.group(1);
        }

        // 2차: 일반 주소에서 동 찾기
        Matcher dongMatcher = DONG_PATTERN.matcher(address);
        if (dongMatcher.find()) {
            return dongMatcher.group(1);
        }

        return null;
    }

    /**
     * 동 이름 정규화 (숫자 제거)
     */
    private String normalizeDongName(String dongName) {
        if (dongName == null) return null;
        return dongName.replaceAll("[0-9]+", "");
    }

    /**
     * 주택형에서 면적 추출 (정수 부분만)
     */
    private BigDecimal extractAreaFromHouseType(String houseType) {
        if (houseType == null) return null;
        try {
            String numStr = houseType.replaceAll("[^0-9].*", "").replaceFirst("^0+", "");
            if (numStr.isEmpty()) return null;
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 주택형별 시세 비교 생성
     */
    private List<HouseTypeComparison> buildHouseTypeComparisons(
            List<SubscriptionPrice> prices,
            List<RealTransaction> transactions) {

        if (prices.isEmpty()) {
            return Collections.emptyList();
        }

        List<HouseTypeComparison> comparisons = new ArrayList<>();

        for (SubscriptionPrice price : prices) {
            BigDecimal exclusiveArea = extractAreaFromHouseType(price.getHouseType());
            if (exclusiveArea == null) continue;

            // 비슷한 면적 범위 (±5㎡)
            BigDecimal FIVE = new BigDecimal("5");
            BigDecimal minArea = exclusiveArea.subtract(FIVE);
            BigDecimal maxArea = exclusiveArea.add(FIVE);

            // 해당 면적대 거래 찾기
            List<RealTransaction> similarAreaTx = transactions.stream()
                    .filter(t -> t.getExclusiveArea() != null)
                    .filter(t -> t.getExclusiveArea().compareTo(minArea) >= 0
                              && t.getExclusiveArea().compareTo(maxArea) <= 0)
                    .sorted((a, b) -> b.getDealDate().compareTo(a.getDealDate()))
                    .limit(5)
                    .toList();

            // 예상 차익 계산 (유사 면적 거래 평균)
            Long marketPrice = null;
            Long estimatedProfit = null;
            if (!similarAreaTx.isEmpty()) {
                marketPrice = (long) similarAreaTx.stream()
                        .mapToLong(RealTransaction::getDealAmount)
                        .average()
                        .orElse(0);
                if (price.getTopAmount() != null) {
                    estimatedProfit = marketPrice - price.getTopAmount();
                }
            }

            comparisons.add(HouseTypeComparison.builder()
                    .houseType(price.getHouseType())
                    .supplyArea(exclusiveArea)
                    .supplyPrice(price.getTopAmount())
                    .marketPrice(marketPrice)
                    .estimatedProfit(estimatedProfit)
                    .similarTransactions(similarAreaTx)
                    .build());
        }

        return comparisons;
    }

    /**
     * 주소에서 법정동코드 추출
     */
    private String extractLawdCd(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 1차: 복합 시군구 패턴 (용인시 수지구 → 용인시수지구)
        Matcher complexMatcher = SIGUNGU_PATTERN.matcher(address);
        if (complexMatcher.find()) {
            String sidoName = complexMatcher.group(1);
            String siName = complexMatcher.group(2);
            String guName = complexMatcher.group(3);
            String sigunguName = siName + guName;

            Optional<String> lawdCd = regionCodeQueryPort.findLawdCdByContaining(sigunguName);
            if (lawdCd.isPresent()) {
                log.debug("복합 시군구 매칭: {} → {}", sigunguName, lawdCd.get());
                return lawdCd.get();
            }
        }

        // 2차: 단순 시군구 패턴 (강남구, 수원시 등)
        Matcher simpleMatcher = SIMPLE_SIGUNGU_PATTERN.matcher(address);
        if (simpleMatcher.find()) {
            String sidoName = simpleMatcher.group(1);
            String sigunguName = simpleMatcher.group(2);

            Optional<String> lawdCd = regionCodeQueryPort.findLawdCd(sidoName, sigunguName);
            if (lawdCd.isPresent()) {
                return lawdCd.get();
            }

            // 부분 일치로 재시도
            lawdCd = regionCodeQueryPort.findLawdCdByContaining(sigunguName);
            if (lawdCd.isPresent()) {
                return lawdCd.get();
            }
        }

        log.debug("법정동코드를 찾을 수 없음: {}", address);
        return null;
    }

    /**
     * 시장 분석
     */
    private MarketAnalysis analyzeMarket(List<RealTransaction> transactions) {
        if (transactions.isEmpty()) {
            return null;
        }

        // 평균 거래가
        long avgAmount = (long) transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .average()
                .orElse(0);

        // 평균 평당가
        long avgPricePerPyeong = (long) transactions.stream()
                .map(RealTransaction::getPricePerPyeong)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        // 최고가
        long maxAmount = transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .max()
                .orElse(0);

        // 최저가
        long minAmount = transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .min()
                .orElse(0);

        // 거래량
        int transactionCount = transactions.size();

        return MarketAnalysis.builder()
                .averageAmount(avgAmount)
                .averagePricePerPyeong(avgPricePerPyeong)
                .maxAmount(maxAmount)
                .minAmount(minAmount)
                .transactionCount(transactionCount)
                .build();
    }
}

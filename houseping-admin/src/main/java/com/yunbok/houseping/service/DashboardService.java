package com.yunbok.houseping.service;

import com.yunbok.houseping.service.dto.DashboardStatisticsDto;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import com.yunbok.houseping.entity.SubscriptionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CompetitionRateRepository competitionRateRepository;

    @Transactional(readOnly = true)
    public DashboardStatisticsDto getStatistics() {
        List<CompetitionRateEntity> allRates = competitionRateRepository.findAll();

        if (allRates.isEmpty()) {
            return emptyStatistics();
        }

        // 통계 계산
        DashboardStatisticsDto.Summary summary = calculateSummary(allRates);
        DashboardStatisticsDto.AreaYearlyTrend areaYearlyTrend = calculateAreaYearlyTrend(allRates);
        List<DashboardStatisticsDto.ResidenceAreaStat> byResidenceArea = calculateByResidenceArea(allRates);
        List<DashboardStatisticsDto.HouseTypeStat> byHouseType = calculateByHouseType(allRates);
        DashboardStatisticsDto.RateDistribution distribution = calculateDistribution(allRates);

        return new DashboardStatisticsDto(summary, areaYearlyTrend, byResidenceArea, byHouseType, distribution);
    }

    private DashboardStatisticsDto.Summary calculateSummary(List<CompetitionRateEntity> rates) {

        // 유효한 경쟁률만 필터링 (20세대 이상, 경쟁률 > 0)
        List<BigDecimal> validRates = rates.stream()
                .filter(this::isValidRateEntity)
                .map(CompetitionRateEntity::getCompetitionRate)
                .toList();

        BigDecimal avgRate = null;
        BigDecimal maxRate = null;
        BigDecimal minRate = null;

        if (!validRates.isEmpty()) {
            BigDecimal sum = validRates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            avgRate = sum.divide(BigDecimal.valueOf(validRates.size()), 2, RoundingMode.HALF_UP);
            maxRate = validRates.stream().max(BigDecimal::compareTo).orElse(null);
            minRate = validRates.stream().min(BigDecimal::compareTo).orElse(null);
        }

        long areaCount = rates.stream()
                .map(r -> r.getSubscription())
                .filter(Objects::nonNull)
                .map(SubscriptionEntity::getArea)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long houseTypeCount = rates.stream()
                .map(CompetitionRateEntity::getHouseType)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        return new DashboardStatisticsDto.Summary(
                rates.size(),
                avgRate,
                maxRate,
                minRate,
                (int) areaCount,
                (int) houseTypeCount
        );
    }

    private DashboardStatisticsDto.AreaYearlyTrend calculateAreaYearlyTrend(List<CompetitionRateEntity> rates) {

        // 유효한 데이터만 필터링 (20세대 이상, 경쟁률 > 0, 지역/연도 정보 있음)
        List<CompetitionRateEntity> validRates = rates.stream()
                .filter(this::isValidRateEntity)
                .filter(r -> {
                    SubscriptionEntity sub = r.getSubscription();
                    return sub != null && sub.getArea() != null && getSubscriptionYear(sub) != null;
                })
                .toList();

        if (validRates.isEmpty()) {
            return new DashboardStatisticsDto.AreaYearlyTrend(List.of(), List.of());
        }

        // 연도 목록 추출 (청약 날짜 기준, 정렬)
        List<Integer> years = validRates.stream()
                .map(r -> getSubscriptionYear(r.getSubscription()))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        // 지역별로 그룹핑
        Map<String, List<CompetitionRateEntity>> byArea = validRates.stream()
                .collect(Collectors.groupingBy(r -> r.getSubscription().getArea()));

        // 지역별 연도별 평균 경쟁률 계산
        List<DashboardStatisticsDto.AreaYearlyData> areaData = byArea.entrySet().stream()
                .map(entry -> {
                    String area = entry.getKey();
                    List<CompetitionRateEntity> areaRates = entry.getValue();

                    // 연도별 평균 계산 (청약 날짜 기준) - 이미 validRates로 필터링됨
                    Map<Integer, BigDecimal> yearlyAvg = areaRates.stream()
                            .collect(Collectors.groupingBy(
                                    r -> getSubscriptionYear(r.getSubscription()),
                                    Collectors.collectingAndThen(
                                            Collectors.toList(),
                                            list -> {
                                                List<BigDecimal> rateValues = list.stream()
                                                        .map(CompetitionRateEntity::getCompetitionRate)
                                                        .toList();
                                                return calculateAverage(rateValues);
                                            }
                                    )
                            ));

                    // years 순서에 맞게 경쟁률 리스트 생성 (없으면 null)
                    List<BigDecimal> ratesByYear = years.stream()
                            .map(yearlyAvg::get)
                            .toList();

                    return new DashboardStatisticsDto.AreaYearlyData(area, ratesByYear);
                })
                .sorted((a, b) -> {
                    // 데이터가 가장 많은 지역 순으로 정렬
                    long countA = a.rates().stream().filter(Objects::nonNull).count();
                    long countB = b.rates().stream().filter(Objects::nonNull).count();
                    return Long.compare(countB, countA);
                })
                .limit(10) // 상위 10개 지역만
                .toList();

        return new DashboardStatisticsDto.AreaYearlyTrend(years, areaData);
    }

    /**
     * 청약의 연도를 반환 (당첨발표일 > 접수시작일 > 공고일 순으로 우선)
     */
    private Integer getSubscriptionYear(SubscriptionEntity sub) {
        if (sub == null) return null;
        if (sub.getWinnerAnnounceDate() != null) return sub.getWinnerAnnounceDate().getYear();
        if (sub.getReceiptStartDate() != null) return sub.getReceiptStartDate().getYear();
        if (sub.getAnnounceDate() != null) return sub.getAnnounceDate().getYear();
        return null;
    }

    private List<DashboardStatisticsDto.ResidenceAreaStat> calculateByResidenceArea(List<CompetitionRateEntity> rates) {
        Map<String, List<CompetitionRateEntity>> byResidence = rates.stream()
                .filter(r -> r.getResidenceArea() != null && !r.getResidenceArea().isBlank())
                .collect(Collectors.groupingBy(CompetitionRateEntity::getResidenceArea));

        return byResidence.entrySet().stream()
                .map(entry -> {
                    String residence = entry.getKey();
                    List<CompetitionRateEntity> residenceRates = entry.getValue();
                    List<BigDecimal> validRates = residenceRates.stream()
                            .map(CompetitionRateEntity::getCompetitionRate)
                            .filter(Objects::nonNull)
                            .toList();

                    BigDecimal avg = calculateAverage(validRates);

                    return new DashboardStatisticsDto.ResidenceAreaStat(residence, residenceRates.size(), avg);
                })
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .toList();
    }

    private List<DashboardStatisticsDto.HouseTypeStat> calculateByHouseType(List<CompetitionRateEntity> rates) {
        // 주택형을 주요 평수(84, 59 등)로 그룹핑
        Map<Integer, List<CompetitionRateEntity>> byMainSize = rates.stream()
                .filter(r -> r.getHouseType() != null && !r.getHouseType().isBlank())
                .filter(r -> extractMainSize(r.getHouseType()) != null)
                .collect(Collectors.groupingBy(r -> extractMainSize(r.getHouseType())));

        return byMainSize.entrySet().stream()
                .map(entry -> {
                    Integer mainSize = entry.getKey();
                    List<CompetitionRateEntity> typeRates = entry.getValue();
                    List<BigDecimal> validRates = typeRates.stream()
                            .map(CompetitionRateEntity::getCompetitionRate)
                            .filter(Objects::nonNull)
                            .toList();

                    BigDecimal avg = calculateAverage(validRates);
                    String houseType = mainSize + "㎡";
                    String sizeCategory = categorizeSqmBySize(mainSize);

                    return new DashboardStatisticsDto.HouseTypeStat(houseType, sizeCategory, typeRates.size(), avg);
                })
                .filter(stat -> stat.count() >= 100) // 100개 이상만
                .sorted(Comparator.comparing(
                        DashboardStatisticsDto.HouseTypeStat::avgRate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    /**
     * 주택형에서 주요 평수 추출 (예: "084.9543T" -> 84)
     */
    private Integer extractMainSize(String houseType) {
        try {
            String numPart = houseType.replaceAll("[^0-9.]", "");
            if (numPart.isEmpty()) return null;
            return (int) Double.parseDouble(numPart.split("\\.")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private String categorizeSqmBySize(int sqm) {
        if (sqm < 60) return "소형 (60㎡ 미만)";
        if (sqm < 85) return "중소형 (60~85㎡)";
        if (sqm < 102) return "중형 (85~102㎡)";
        if (sqm < 135) return "중대형 (102~135㎡)";
        return "대형 (135㎡ 이상)";
    }

    private DashboardStatisticsDto.RateDistribution calculateDistribution(List<CompetitionRateEntity> rates) {
        long under5 = 0, from5to10 = 0, from10to20 = 0, from20to50 = 0, over50 = 0;

        for (CompetitionRateEntity rate : rates) {
            if (!isValidRateEntity(rate)) continue;

            double r = rate.getCompetitionRate().doubleValue();
            if (r <= 5) under5++;
            else if (r <= 10) from5to10++;
            else if (r <= 20) from10to20++;
            else if (r <= 50) from20to50++;
            else over50++;
        }

        return new DashboardStatisticsDto.RateDistribution(under5, from5to10, from10to20, from20to50, over50);
    }

    /**
     * 유효한 경쟁률 데이터인지 확인
     * - 경쟁률: 0 초과
     * - 공급세대수: 20세대 이상 (소규모 이상치 제외)
     */
    private boolean isValidRateEntity(CompetitionRateEntity entity) {
        if (entity == null) return false;
        BigDecimal rate = entity.getCompetitionRate();
        Integer supply = entity.getSupplyCount();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (supply == null || supply < 20) return false;
        return true;
    }

    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private DashboardStatisticsDto emptyStatistics() {
        return new DashboardStatisticsDto(
                new DashboardStatisticsDto.Summary(0, null, null, null, 0, 0),
                new DashboardStatisticsDto.AreaYearlyTrend(List.of(), List.of()),
                List.of(),
                List.of(),
                new DashboardStatisticsDto.RateDistribution(0, 0, 0, 0, 0)
        );
    }
}

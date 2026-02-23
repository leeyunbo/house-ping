package com.yunbok.houseping.core.service.competition;

import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.infrastructure.persistence.CompetitionRateDbStore;
import com.yunbok.houseping.core.port.CompetitionRateProvider;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 경쟁률 수집 서비스
 * 청약홈 API에서 경쟁률 전체 조회 후 서울/경기만 필터링하여 DB 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionRateCollectorService {

    private final CompetitionRateDbStore competitionRatePort;
    private final SubscriptionStore subscriptionPort;
    private final Optional<CompetitionRateProvider> competitionRateProvider;
    private final SubscriptionConfig config;

    /**
     * 경쟁률 수집
     * - API에서 전체 조회
     * - 서울/경기 지역만 필터링
     * - 중복 제외하고 신규 건만 저장
     *
     * @return 신규 저장 건수
     */
    @Transactional
    public int collect() {
        if (competitionRateProvider.isEmpty()) {
            log.info("[경쟁률 수집] Provider가 비활성화 상태입니다.");
            return 0;
        }

        log.info("[경쟁률 수집] 시작 - 대상 지역: {}", config.targetAreas());

        Set<String> targetHouseManageNumbers = findTargetHouseManageNumbers();
        if (targetHouseManageNumbers.isEmpty()) {
            log.warn("[경쟁률 수집] 대상 지역에 청약 데이터가 없습니다. 먼저 청약 데이터를 동기화하세요.");
            return 0;
        }

        List<CompetitionRate> allRates = fetchAllRates();
        List<CompetitionRate> newRates = filterNewRates(allRates, targetHouseManageNumbers);

        return saveRates(newRates);
    }

    private Set<String> findTargetHouseManageNumbers() {
        Set<String> nos = subscriptionPort.findHouseManageNosByAreas(config.targetAreas());
        log.info("[경쟁률 수집] 대상 지역 청약 건수: {}", nos.size());
        return nos;
    }

    private List<CompetitionRate> fetchAllRates() {
        List<CompetitionRate> rates = competitionRateProvider.get().fetchAll();
        log.info("[경쟁률 수집] API 조회 완료 - 전체 {}건", rates.size());
        return rates;
    }

    private List<CompetitionRate> filterNewRates(List<CompetitionRate> allRates, Set<String> targetHouseManageNos) {
        List<CompetitionRate> newRates = allRates.stream()
                .filter(rate -> targetHouseManageNos.contains(rate.getHouseManageNo()))
                .filter(rate -> !competitionRatePort.existsByHouseManageNoAndPblancNo(
                        rate.getHouseManageNo(), rate.getPblancNo()))
                .toList();
        log.info("[경쟁률 수집] 필터링 후 신규 데이터: {}건", newRates.size());
        return newRates;
    }

    private int saveRates(List<CompetitionRate> rates) {
        if (rates.isEmpty()) {
            log.info("[경쟁률 수집] 신규 데이터 없음");
            return 0;
        }
        competitionRatePort.saveAll(rates);
        log.info("[경쟁률 수집] 완료 - {}건 저장", rates.size());
        return rates.size();
    }
}

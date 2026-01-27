package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.CompetitionRate;
import com.yunbok.houseping.domain.model.SubscriptionConfig;
import com.yunbok.houseping.domain.port.out.CompetitionRatePersistencePort;
import com.yunbok.houseping.domain.port.out.CompetitionRateProvider;
import com.yunbok.houseping.domain.port.out.SubscriptionPersistencePort;
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

    private final CompetitionRatePersistencePort competitionRatePort;
    private final SubscriptionPersistencePort subscriptionPort;
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

        // 1. 서울/경기 지역의 house_manage_no 목록 조회
        Set<String> targetHouseManageNos = subscriptionPort.findHouseManageNosByAreas(config.targetAreas());
        log.info("[경쟁률 수집] 대상 지역 청약 건수: {}", targetHouseManageNos.size());

        if (targetHouseManageNos.isEmpty()) {
            log.warn("[경쟁률 수집] 대상 지역에 청약 데이터가 없습니다. 먼저 청약 데이터를 동기화하세요.");
            return 0;
        }

        // 2. API에서 전체 조회
        List<CompetitionRate> allRates = competitionRateProvider.get().fetchAll();
        log.info("[경쟁률 수집] API 조회 완료 - 전체 {}건", allRates.size());

        // 3. 서울/경기 지역만 필터링 + 중복 제외
        List<CompetitionRate> filteredRates = allRates.stream()
                .filter(rate -> targetHouseManageNos.contains(rate.getHouseManageNo()))
                .filter(rate -> !competitionRatePort.existsByHouseManageNoAndPblancNo(
                        rate.getHouseManageNo(), rate.getPblancNo()))
                .toList();

        log.info("[경쟁률 수집] 필터링 후 신규 데이터: {}건", filteredRates.size());

        if (filteredRates.isEmpty()) {
            log.info("[경쟁률 수집] 신규 데이터 없음");
            return 0;
        }

        // 4. DB 저장
        competitionRatePort.saveAll(filteredRates);
        log.info("[경쟁률 수집] 완료 - {}건 저장", filteredRates.size());

        return filteredRates.size();
    }
}

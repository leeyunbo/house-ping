package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.LhSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.infrastructure.adapter.outbound.api.ApplyhomeApiAdapter;
import com.yunbok.houseping.infrastructure.adapter.outbound.api.LhApiAdapter;
import com.yunbok.houseping.infrastructure.persistence.entity.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 청약 정보 동기화 서비스
 * API에서 데이터를 수집하여 DB에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionSyncService {

    private final SubscriptionRepository subscriptionRepository;
    private final ApplyhomeApiAdapter applyhomeApiAdapter;
    private final LhApiAdapter lhApiAdapter;

    @Value("${sync.initial-range-months:12}")
    private int initialRangeMonths;

    @Value("${sync.daily-range-months:1}")
    private int dailyRangeMonths;

    private static final List<String> TARGET_AREAS = Arrays.asList("서울", "경기");

    /**
     * 초기 데이터 로드 (향후 12개월)
     */
    @Transactional
    public SyncResult syncInitialData() {
        log.info("🔄 초기 데이터 동기화 시작 (향후 {}개월)", initialRangeMonths);

        return syncDataForPeriod();
    }

    /**
     * 특정 기간 데이터 동기화
     */
    private SyncResult syncDataForPeriod() {
        int totalInserted = 0;
        int totalUpdated = 0;
        int totalSkipped = 0;

        // 지역별로 한번에 모든 데이터 수집 (날짜별 루프 제거)
        for (String area : TARGET_AREAS) {
            // 청약Home API 동기화
            try {
                List<SubscriptionInfo> applyhomeData = applyhomeApiAdapter.fetchAll(area);
                SyncResult applyhomeResult = saveSubscriptions(applyhomeData, "APPLYHOME_API");
                totalInserted += applyhomeResult.inserted;
                totalUpdated += applyhomeResult.updated;
                totalSkipped += applyhomeResult.skipped;
                log.info("[청약Home API] {} 지역 동기화 완료 - 추가: {}, 업데이트: {}, 스킵: {}",
                        area, applyhomeResult.inserted, applyhomeResult.updated, applyhomeResult.skipped);
            } catch (Exception e) {
                log.warn("[청약Home API] {} 동기화 실패: {}", area, e.getMessage());
            }

            // LH API 동기화
            try {
                List<SubscriptionInfo> lhData = lhApiAdapter.fetchAll(area);
                SyncResult lhResult = saveSubscriptions(lhData, "LH_API");
                totalInserted += lhResult.inserted;
                totalUpdated += lhResult.updated;
                totalSkipped += lhResult.skipped;
                log.info("[LH API] {} 지역 동기화 완료 - 추가: {}, 업데이트: {}, 스킵: {}",
                        area, lhResult.inserted, lhResult.updated, lhResult.skipped);
            } catch (Exception e) {
                log.warn("[LH API] {} 동기화 실패: {}", area, e.getMessage());
            }
        }

        SyncResult result = new SyncResult(totalInserted, totalUpdated, totalSkipped);
        log.info("✅ 데이터 동기화 완료 - 추가: {}, 업데이트: {}, 스킵: {}", result.inserted, result.updated, result.skipped);

        return result;
    }

    /**
     * 청약 정보 리스트를 DB에 저장
     */
    private SyncResult saveSubscriptions(List<SubscriptionInfo> subscriptions, String source) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (SubscriptionInfo info : subscriptions) {
            try {
                SubscriptionEntity entity = toEntity(info, source);

                // 중복 체크
                Optional<SubscriptionEntity> existing = subscriptionRepository
                        .findBySourceAndHouseNameAndReceiptStartDate(
                                entity.getSource(),
                                entity.getHouseName(),
                                entity.getReceiptStartDate()
                        );

                if (existing.isPresent()) {
                    SubscriptionEntity existingEntity = existing.get();
                    // 변경사항이 있으면 업데이트
                    if (existingEntity.needsUpdate(entity)) {
                        existingEntity.updateFrom(entity);
                        subscriptionRepository.save(existingEntity);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    // 신규 저장
                    subscriptionRepository.save(entity);
                    inserted++;
                }
            } catch (Exception e) {
                log.warn("청약 정보 저장 실패: {} - {}", info.getDisplayMessage(), e.getMessage());
            }
        }

        return new SyncResult(inserted, updated, skipped);
    }

    /**
     * SubscriptionInfo를 Entity로 변환
     */
    private SubscriptionEntity toEntity(SubscriptionInfo info, String source) {
        if (info instanceof ApplyHomeSubscriptionInfo applyHomeInfo) {
            return SubscriptionEntity.builder()
                    .source(source)
                    .houseName(applyHomeInfo.getHouseName())
                    .houseType(applyHomeInfo.getHouseType())
                    .area(applyHomeInfo.getArea())
                    .announceDate(applyHomeInfo.getAnnounceDate())
                    .receiptStartDate(applyHomeInfo.getReceiptStartDate())
                    .receiptEndDate(applyHomeInfo.getReceiptEndDate())
                    .winnerAnnounceDate(applyHomeInfo.getWinnerAnnounceDate())
                    .detailUrl(applyHomeInfo.getDetailUrl())
                    .homepageUrl(applyHomeInfo.getHomepageUrl())
                    .contact(applyHomeInfo.getContact())
                    .totalSupplyCount(applyHomeInfo.getTotalSupplyCount())
                    .collectedAt(LocalDateTime.now())
                    .build();
        } else if (info instanceof LhSubscriptionInfo lhInfo) {
            return SubscriptionEntity.builder()
                    .source(source)
                    .houseName(lhInfo.getHouseName())
                    .houseType(lhInfo.getHouseType())
                    .area(lhInfo.getArea())
                    .announceDate(lhInfo.getAnnounceDate())
                    .receiptStartDate(lhInfo.getAnnounceDate()) // LH는 announce date를 receipt start로 사용
                    .receiptEndDate(lhInfo.getReceiptEndDate())
                    .detailUrl(lhInfo.getDetailUrl())
                    .collectedAt(LocalDateTime.now())
                    .build();
        } else {
            throw new IllegalArgumentException("Unknown SubscriptionInfo type: " + info.getClass());
        }
    }

    /**
     * 오래된 데이터 정리 (1년 이상 지난 데이터)
     */
    @Transactional
    public int cleanupOldData() {
        LocalDate cutoffDate = LocalDate.now().minusYears(1);
        int deletedCount = subscriptionRepository.deleteOldSubscriptions(cutoffDate);
        log.info("🗑️ 오래된 데이터 {}건 삭제 완료 (기준일: {})", deletedCount, cutoffDate);
        return deletedCount;
    }

    /**
     * 동기화 결과 DTO
     */
    public static class SyncResult {
        public final int inserted;
        public final int updated;
        public final int skipped;

        public SyncResult(int inserted, int updated, int skipped) {
            this.inserted = inserted;
            this.updated = updated;
            this.skipped = skipped;
        }

        public int total() {
            return inserted + updated + skipped;
        }
    }
}

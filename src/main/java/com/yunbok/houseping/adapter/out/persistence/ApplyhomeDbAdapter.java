package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.SubscriptionProvider;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.subscription.applyhome-db-enabled",
    havingValue = "true",
    matchIfMissing = false
)
@RequiredArgsConstructor
public class ApplyhomeDbAdapter implements SubscriptionProvider {

    private static final String SOURCE_PREFIX = "ApplyHome";
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[청약Home DB] {} 지역 {} 접수 시작 청약 조회", areaName, targetDate);

            List<SubscriptionEntity> entities = subscriptionRepository
                    .findByAreaAndReceiptStartDate(areaName, targetDate);

            // 청약Home 소스만 필터링
            List<SubscriptionInfo> subscriptions = entities.stream()
                    .filter(entity -> entity.getSource().startsWith(SOURCE_PREFIX))
                    .map(this::toSubscriptionInfo)
                    .collect(Collectors.toList());

            log.info("[청약Home DB] {} 지역에서 {}개 데이터 조회 완료", areaName, subscriptions.size());
            return subscriptions;

        } catch (Exception e) {
            log.error("[청약Home DB] 데이터 조회 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Entity를 SubscriptionInfo로 변환
     */
    private SubscriptionInfo toSubscriptionInfo(SubscriptionEntity entity) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(entity.getHouseName())
                .houseType(entity.getHouseType())
                .area(entity.getArea())
                .announceDate(entity.getAnnounceDate())
                .receiptStartDate(entity.getReceiptStartDate())
                .receiptEndDate(entity.getReceiptEndDate())
                .winnerAnnounceDate(entity.getWinnerAnnounceDate())
                .detailUrl(entity.getDetailUrl())
                .homepageUrl(entity.getHomepageUrl())
                .contact(entity.getContact())
                .totalSupplyCount(entity.getTotalSupplyCount())
                .address(entity.getAddress())
                .zipCode(entity.getZipCode())
                .build();
    }
}

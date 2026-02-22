package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
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
public class ApplyhomeDbStore implements SubscriptionProvider {

    private static final String SOURCE_PREFIX = SubscriptionSource.APPLYHOME.getValue();
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public boolean isExternalSource() {
        return false;
    }

    public List<Subscription> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[청약Home DB] {} 지역 {} 접수 시작 청약 조회", areaName, targetDate);

            List<SubscriptionEntity> entities = subscriptionRepository
                    .findByAreaAndReceiptStartDate(areaName, targetDate);

            List<Subscription> subscriptions = entities.stream()
                    .filter(entity -> entity.getSource().startsWith(SOURCE_PREFIX))
                    .map(this::toDomain)
                    .collect(Collectors.toList());

            log.info("[청약Home DB] {} 지역에서 {}개 데이터 조회 완료", areaName, subscriptions.size());
            return subscriptions;

        } catch (Exception e) {
            log.error("[청약Home DB] 데이터 조회 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Subscription toDomain(SubscriptionEntity entity) {
        return Subscription.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .houseManageNo(entity.getHouseManageNo())
                .pblancNo(entity.getPblancNo())
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

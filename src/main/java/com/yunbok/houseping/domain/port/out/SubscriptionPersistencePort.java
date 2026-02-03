package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface SubscriptionPersistencePort {

    Optional<SubscriptionInfo> findBySourceAndHouseNameAndReceiptStartDate(
            String source, String houseName, LocalDate receiptStartDate);

    void save(SubscriptionInfo info, String source);

    void update(SubscriptionInfo info, String source);

    int deleteOldSubscriptions(LocalDate cutoffDate);

    /**
     * 특정 지역들의 house_manage_no 목록 조회
     */
    Set<String> findHouseManageNosByAreas(java.util.List<String> areas);
}

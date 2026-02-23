package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.Subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubscriptionPersistencePort {

    Optional<Subscription> findById(Long id);

    List<Subscription> findByAreaContaining(String area);

    List<Subscription> findBySourceAndAreas(String source, List<String> areas);

    List<Subscription> findBySupportedAreas(List<String> areas);

    List<Subscription> findByReceiptStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<Subscription> findByReceiptPeriodOverlapping(LocalDate weekStart, LocalDate weekEnd);

    List<Subscription> findAll();

    List<Subscription> findRecentSubscriptions(int limit);

    Optional<Subscription> findBySourceAndHouseNameAndReceiptStartDate(String source, String houseName, LocalDate receiptStartDate);

    List<Subscription> findByAreaAndReceiptStartDate(String area, LocalDate receiptStartDate);

    Set<String> findHouseManageNosByAreas(List<String> areas);

    void save(Subscription subscription, String source);

    void update(Subscription subscription, String source);

    int deleteOldSubscriptions(LocalDate cutoffDate);
}

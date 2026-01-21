package com.yunbok.houseping.domain.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 청약 정보 도메인 모델
 */
public interface SubscriptionInfo {

    // === 표시 메시지 ===
    String getDisplayMessage();
    String getSimpleDisplayMessage();

    // === 필수 필드 ===
    String getHouseName();
    String getHouseType();
    String getArea();
    LocalDate getAnnounceDate();
    LocalDate getReceiptEndDate();
    String getDetailUrl();

    // === 선택적 필드 (기본값 제공) ===
    default LocalDate getReceiptStartDate() { return getAnnounceDate(); }
    default LocalDate getWinnerAnnounceDate() { return null; }
    default String getHomepageUrl() { return null; }
    default String getContact() { return null; }
    default Integer getTotalSupplyCount() { return null; }

    // === Optional 반환 (null-safety) ===
    default Optional<LocalDate> getWinnerAnnounceDateOpt() {
        return Optional.ofNullable(getWinnerAnnounceDate());
    }

    default Optional<String> getHomepageUrlOpt() {
        return Optional.ofNullable(getHomepageUrl());
    }

    default Optional<String> getContactOpt() {
        return Optional.ofNullable(getContact());
    }

    default Optional<Integer> getTotalSupplyCountOpt() {
        return Optional.ofNullable(getTotalSupplyCount());
    }

    // === 도메인 로직 ===

    /**
     * 청약 접수가 진행 중인지 확인
     */
    default boolean isReceiptInProgress() {
        LocalDate today = LocalDate.now();
        LocalDate start = getReceiptStartDate();
        LocalDate end = getReceiptEndDate();
        return start != null && !today.isBefore(start)
            && (end == null || !today.isAfter(end));
    }

    /**
     * 청약 접수 예정인지 확인
     */
    default boolean isUpcoming() {
        LocalDate today = LocalDate.now();
        LocalDate start = getReceiptStartDate();
        return start != null && today.isBefore(start);
    }

    /**
     * 청약 접수가 마감되었는지 확인
     */
    default boolean isExpired() {
        LocalDate today = LocalDate.now();
        LocalDate end = getReceiptEndDate();
        return end != null && today.isAfter(end);
    }

    /**
     * 유효한 청약 정보인지 검증
     */
    default boolean isValid() {
        return getHouseName() != null && !getHouseName().isBlank()
            && getArea() != null && !getArea().isBlank();
    }

    /**
     * 대규모 공급인지 확인 (100세대 이상)
     */
    default boolean isLargeSupply() {
        Integer count = getTotalSupplyCount();
        return count != null && count >= 100;
    }
}

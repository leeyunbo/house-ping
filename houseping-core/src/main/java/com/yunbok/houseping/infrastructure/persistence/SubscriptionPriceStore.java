package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.entity.SubscriptionPriceEntity;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 분양가 조회 어댑터
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPriceStore {

    private final SubscriptionPriceRepository subscriptionPriceRepository;

    public List<SubscriptionPrice> findByHouseManageNo(String houseManageNo) {
        return subscriptionPriceRepository.findByHouseManageNo(houseManageNo).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Entity -> Domain Model 변환
     */
    private SubscriptionPrice toDomain(SubscriptionPriceEntity entity) {
        return SubscriptionPrice.builder()
                .id(entity.getId())
                .houseManageNo(entity.getHouseManageNo())
                .pblancNo(entity.getPblancNo())
                .modelNo(entity.getModelNo())
                .houseType(entity.getHouseType())
                .supplyArea(entity.getSupplyArea())
                .supplyCount(entity.getSupplyCount())
                .specialSupplyCount(entity.getSpecialSupplyCount())
                .topAmount(entity.getTopAmount())
                .pricePerPyeong(entity.getPricePerPyeong())
                .build();
    }
}

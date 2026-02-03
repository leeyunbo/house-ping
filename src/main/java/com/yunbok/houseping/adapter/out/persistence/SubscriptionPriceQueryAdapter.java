package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.domain.model.SubscriptionPrice;
import com.yunbok.houseping.domain.port.out.SubscriptionPriceQueryPort;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionPriceEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 분양가 조회 어댑터
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPriceQueryAdapter implements SubscriptionPriceQueryPort {

    private final SubscriptionPriceRepository subscriptionPriceRepository;

    @Override
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

package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.entity.RegionCodeEntity;
import com.yunbok.houseping.repository.RegionCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 지역코드 조회 어댑터
 */
@Component
@RequiredArgsConstructor
public class RegionCodeQueryAdapter {

    private final RegionCodeRepository regionCodeRepository;

    public Optional<String> findLawdCd(String sidoName, String sigunguName) {
        return regionCodeRepository.findBySidoNameAndSigunguName(sidoName, sigunguName)
                .map(RegionCodeEntity::getLawdCd);
    }

    public Optional<String> findLawdCdByContaining(String sigunguName) {
        List<RegionCodeEntity> candidates = regionCodeRepository.findBySigunguNameContaining(sigunguName);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(0).getLawdCd());
    }
}

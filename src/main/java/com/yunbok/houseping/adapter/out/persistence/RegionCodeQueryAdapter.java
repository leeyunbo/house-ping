package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.domain.port.out.RegionCodeQueryPort;
import com.yunbok.houseping.infrastructure.persistence.RegionCodeEntity;
import com.yunbok.houseping.infrastructure.persistence.RegionCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 지역코드 조회 어댑터
 */
@Component
@RequiredArgsConstructor
public class RegionCodeQueryAdapter implements RegionCodeQueryPort {

    private final RegionCodeRepository regionCodeRepository;

    @Override
    public Optional<String> findLawdCd(String sidoName, String sigunguName) {
        return regionCodeRepository.findBySidoNameAndSigunguName(sidoName, sigunguName)
                .map(RegionCodeEntity::getLawdCd);
    }

    @Override
    public Optional<String> findLawdCdByContaining(String sigunguName) {
        List<RegionCodeEntity> candidates = regionCodeRepository.findBySigunguNameContaining(sigunguName);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(0).getLawdCd());
    }
}

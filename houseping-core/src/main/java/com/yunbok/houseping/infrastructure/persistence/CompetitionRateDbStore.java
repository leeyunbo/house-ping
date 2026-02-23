package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 경쟁률 DB 어댑터
 */
@Component
@RequiredArgsConstructor
public class CompetitionRateDbStore {

    private final CompetitionRateRepository repository;

    public void save(CompetitionRate competitionRate) {
        CompetitionRateEntity entity = toEntity(competitionRate);
        repository.save(entity);
    }

    public void saveAll(List<CompetitionRate> competitionRates) {
        List<CompetitionRateEntity> entities = competitionRates.stream()
                .map(this::toEntity)
                .toList();
        repository.saveAll(entities);
    }

    public List<CompetitionRate> findByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo) {
        return repository.findByHouseManageNoAndPblancNo(houseManageNo, pblancNo).stream()
                .map(this::toDomain)
                .toList();
    }

    public boolean existsByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo) {
        return repository.existsByHouseManageNoAndPblancNo(houseManageNo, pblancNo);
    }

    private CompetitionRateEntity toEntity(CompetitionRate domain) {
        return CompetitionRateEntity.builder()
                .houseManageNo(domain.getHouseManageNo())
                .pblancNo(domain.getPblancNo())
                .houseType(domain.getHouseType())
                .supplyCount(domain.getSupplyCount())
                .requestCount(domain.getRequestCount())
                .competitionRate(domain.getCompetitionRate())
                .residenceArea(domain.getResidenceArea())
                .rank(domain.getRank())
                .collectedAt(LocalDateTime.now())
                .build();
    }

    private CompetitionRate toDomain(CompetitionRateEntity entity) {
        return CompetitionRate.builder()
                .houseManageNo(entity.getHouseManageNo())
                .pblancNo(entity.getPblancNo())
                .houseType(entity.getHouseType())
                .supplyCount(entity.getSupplyCount())
                .requestCount(entity.getRequestCount())
                .competitionRate(entity.getCompetitionRate())
                .residenceArea(entity.getResidenceArea())
                .rank(entity.getRank())
                .build();
    }
}

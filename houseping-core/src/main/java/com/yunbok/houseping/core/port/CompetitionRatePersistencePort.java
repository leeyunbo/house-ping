package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.CompetitionRate;

import java.util.List;

public interface CompetitionRatePersistencePort {

    void save(CompetitionRate competitionRate);

    void saveAll(List<CompetitionRate> competitionRates);

    List<CompetitionRate> findByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    boolean existsByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);
}

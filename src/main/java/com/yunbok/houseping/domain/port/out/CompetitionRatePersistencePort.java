package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.CompetitionRate;

import java.util.List;

/**
 * 경쟁률 데이터 저장 포트
 */
public interface CompetitionRatePersistencePort {

    /**
     * 경쟁률 데이터 저장
     *
     * @param competitionRate 경쟁률 정보
     */
    void save(CompetitionRate competitionRate);

    /**
     * 경쟁률 데이터 일괄 저장
     *
     * @param competitionRates 경쟁률 목록
     */
    void saveAll(List<CompetitionRate> competitionRates);

    /**
     * 특정 청약 건의 경쟁률 조회
     *
     * @param houseManageNo 주택관리번호
     * @param pblancNo 공고번호
     * @return 경쟁률 목록
     */
    List<CompetitionRate> findByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);

    /**
     * 경쟁률 데이터 존재 여부 확인
     *
     * @param houseManageNo 주택관리번호
     * @param pblancNo 공고번호
     * @return 존재 여부
     */
    boolean existsByHouseManageNoAndPblancNo(String houseManageNo, String pblancNo);
}

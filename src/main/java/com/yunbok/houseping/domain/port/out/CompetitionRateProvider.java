package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.CompetitionRate;

import java.util.List;

/**
 * 경쟁률 데이터 조회 포트
 */
public interface CompetitionRateProvider {

    /**
     * 전체 경쟁률 조회
     * (당첨 발표 완료된 건만 API에서 데이터 제공)
     *
     * @return 경쟁률 목록
     */
    List<CompetitionRate> fetchAll();
}

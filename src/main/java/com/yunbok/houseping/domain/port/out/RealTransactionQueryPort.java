package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.RealTransaction;

import java.util.List;

/**
 * 실거래가 조회 Port
 */
public interface RealTransactionQueryPort {

    /**
     * 법정동코드와 동 이름으로 실거래 목록 조회 (최근 거래 순)
     */
    List<RealTransaction> findByLawdCdAndDongName(String lawdCd, String dongName);

    /**
     * 법정동코드로 실거래 목록 조회 (최근 거래 순)
     */
    List<RealTransaction> findByLawdCd(String lawdCd);

    /**
     * 캐시된 실거래 데이터 존재 여부 확인
     */
    boolean hasCachedData(String lawdCd);
}

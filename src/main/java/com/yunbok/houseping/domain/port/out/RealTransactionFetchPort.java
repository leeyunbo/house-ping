package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.RealTransaction;

import java.util.List;

/**
 * 실거래가 외부 API 조회 Port
 */
public interface RealTransactionFetchPort {

    /**
     * 법정동코드로 최근 N개월 실거래가 조회 후 캐시 저장
     * @return 조회된 거래 목록
     */
    List<RealTransaction> fetchAndCacheRecentTransactions(String lawdCd, int months);
}

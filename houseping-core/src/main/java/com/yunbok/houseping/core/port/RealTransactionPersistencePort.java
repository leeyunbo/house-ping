package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.RealTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface RealTransactionPersistencePort {

    List<RealTransaction> findByLawdCdAndDongName(String lawdCd, String dongName);

    List<RealTransaction> findByLawdCd(String lawdCd);

    /**
     * 가격 배지 계산용 — DB 단계에서 동/신축/면적까지 모두 거른 결과만 반환.
     * 메인페이지 OOM 방지를 위해 자바 메모리에 lawdCd 전체를 적재하지 않는다.
     *
     * @param lawdCd          법정동코드 5자리
     * @param dongNamePrefix  정규화된 동 이름 (예: "당산동" → "당산동", "당산동1가" 등 모두 매칭)
     * @param buildYearMin    신축 기준 준공년도 (이상)
     * @param minArea         전용면적 하한 (포함)
     * @param maxArea         전용면적 상한 (포함)
     */
    List<RealTransaction> findByLawdCdAndDongPrefixAndAreaRange(
            String lawdCd,
            String dongNamePrefix,
            int buildYearMin,
            BigDecimal minArea,
            BigDecimal maxArea
    );

    boolean hasCachedData(String lawdCd);
}

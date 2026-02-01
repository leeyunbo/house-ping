package com.yunbok.houseping.domain.port.in;

import com.yunbok.houseping.domain.model.SubscriptionAnalysisResult;

/**
 * 청약 분석 UseCase
 */
public interface SubscriptionAnalysisUseCase {

    /**
     * 청약 분석 수행
     * @param subscriptionId 청약 ID
     * @return 분석 결과
     * @throws IllegalArgumentException 청약을 찾을 수 없는 경우
     */
    SubscriptionAnalysisResult analyze(Long subscriptionId);
}

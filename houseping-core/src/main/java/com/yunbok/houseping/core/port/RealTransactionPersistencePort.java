package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.RealTransaction;

import java.util.List;

public interface RealTransactionPersistencePort {

    List<RealTransaction> findByLawdCdAndDongName(String lawdCd, String dongName);

    List<RealTransaction> findByLawdCd(String lawdCd);

    boolean hasCachedData(String lawdCd);
}

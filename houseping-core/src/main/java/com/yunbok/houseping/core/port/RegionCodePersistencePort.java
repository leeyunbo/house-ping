package com.yunbok.houseping.core.port;

import java.util.Optional;

public interface RegionCodePersistencePort {

    Optional<String> findLawdCd(String sidoName, String sigunguName);

    Optional<String> findLawdCdByContaining(String sigunguName);
}

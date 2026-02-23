package com.yunbok.houseping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("HousepingApplication - 애플리케이션 컨텍스트 로딩")
class HousepingApplicationTest {

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
        // 컨텍스트 로딩 확인
    }
}

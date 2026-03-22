package com.yunbok.houseping.support.util;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.port.RegionCodePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@DisplayName("AddressHelper - 주소 파싱 및 동 이름 처리")
@ExtendWith(MockitoExtension.class)
class AddressHelperTest {

    @Mock
    private RegionCodePersistencePort regionCodeQueryPort;

    private AddressHelper addressHelper;

    @BeforeEach
    void setUp() {
        addressHelper = new AddressHelper(regionCodeQueryPort);
    }

    @Nested
    @DisplayName("extractDongName() - 주소에서 동 이름 추출")
    class ExtractDongName {

        @Test
        @DisplayName("일반 주소에서 동 이름을 추출한다")
        void extractsDongFromAddress() {
            // given
            String address = "경기도 용인시 수지구 상현동 123";

            // when
            String result = addressHelper.extractDongName(address);

            // then
            assertThat(result).isEqualTo("상현동");
        }

        @Test
        @DisplayName("괄호 안에서 동 이름을 우선 추출한다")
        void extractsDongFromParentheses() {
            // given
            String address = "서울시 강남구 역삼동 123 (역삼동, 래미안)";

            // when
            String result = addressHelper.extractDongName(address);

            // then
            assertThat(result).isEqualTo("역삼동");
        }

        @Test
        @DisplayName("null을 입력하면 null을 반환한다")
        void returnsNullForNull() {
            // when
            String result = addressHelper.extractDongName(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열을 입력하면 null을 반환한다")
        void returnsNullForBlank() {
            // when
            String result = addressHelper.extractDongName("   ");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("동이 없는 주소면 null을 반환한다")
        void returnsNullWhenNoDong() {
            // given
            String address = "경기도 용인시 수지구 123번지";

            // when
            String result = addressHelper.extractDongName(address);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("'동+숫자+가' 패턴 주소에서 동 이름을 추출한다 (문래동5가 → 문래동)")
        void extractsDongFromDongGaPattern() {
            String result = addressHelper.extractDongName("서울특별시 영등포구 문래동5가 22번지 일원");
            assertThat(result).isEqualTo("문래동");
        }
    }

    @Nested
    @DisplayName("normalizeDongName() - 동 이름 정규화")
    class NormalizeDongName {

        @Test
        @DisplayName("숫자가 포함된 동 이름에서 숫자를 제거한다")
        void removesNumberFromDongName() {
            // when
            String result = addressHelper.normalizeDongName("역삼1동");

            // then
            assertThat(result).isEqualTo("역삼동");
        }

        @Test
        @DisplayName("null을 입력하면 null을 반환한다")
        void returnsNullForNull() {
            // when
            String result = addressHelper.normalizeDongName(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("숫자가 없는 동 이름은 그대로 반환한다")
        void returnsAsIsWithoutNumber() {
            // when
            String result = addressHelper.normalizeDongName("상현동");

            // then
            assertThat(result).isEqualTo("상현동");
        }

        @Test
        @DisplayName("'동+숫자+가' 패턴에서 숫자와 가를 제거한다 (문래동5가 → 문래동)")
        void removesDongGaPattern() {
            assertThat(addressHelper.normalizeDongName("문래동5가")).isEqualTo("문래동");
            assertThat(addressHelper.normalizeDongName("당산동3가")).isEqualTo("당산동");
            assertThat(addressHelper.normalizeDongName("을지로동2가")).isEqualTo("을지로동");
        }
    }

    @Nested
    @DisplayName("filterByDongName() - 동 이름으로 거래 내역 필터링")
    class FilterByDongName {

        @Test
        @DisplayName("동 이름이 일치하는 거래만 필터링한다")
        void filtersByMatchingDongName() {
            // given
            List<RealTransaction> transactions = List.of(
                    createTransaction("역삼1동"),
                    createTransaction("삼성동"),
                    createTransaction("역삼2동")
            );

            // when
            List<RealTransaction> result = addressHelper.filterByDongName(transactions, "역삼동");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allSatisfy(t ->
                    assertThat(t.getDongName()).contains("역삼"));
        }

        @Test
        @DisplayName("dongName이 null이면 전체 거래를 반환한다")
        void returnsAllWhenDongNameIsNull() {
            // given
            List<RealTransaction> transactions = List.of(
                    createTransaction("역삼동"),
                    createTransaction("삼성동")
            );

            // when
            List<RealTransaction> result = addressHelper.filterByDongName(transactions, null);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("빈 리스트를 입력하면 빈 리스트를 반환한다")
        void returnsEmptyForEmptyList() {
            // when
            List<RealTransaction> result = addressHelper.filterByDongName(List.of(), "역삼동");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractLawdCd() - 주소에서 법정동코드 추출")
    class ExtractLawdCd {

        @Test
        @DisplayName("복합 시군구 주소에서 containing으로 법정동코드를 찾는다")
        void findsLawdCdForComplexSigungu() {
            // given
            String address = "경기도 용인시 수지구 상현동 123";
            given(regionCodeQueryPort.findLawdCdByContaining("용인시수지구"))
                    .willReturn(Optional.of("41465"));

            // when
            String result = addressHelper.extractLawdCd(address);

            // then
            assertThat(result).isEqualTo("41465");
        }

        @Test
        @DisplayName("단순 시군구 주소에서 정확히 법정동코드를 찾는다")
        void findsLawdCdForSimpleSigungu() {
            // given
            String address = "서울특별시 강남구 역삼동 123";
            given(regionCodeQueryPort.findLawdCd("서울특별시", "강남구"))
                    .willReturn(Optional.of("11680"));

            // when
            String result = addressHelper.extractLawdCd(address);

            // then
            assertThat(result).isEqualTo("11680");
        }

        @Test
        @DisplayName("null을 입력하면 null을 반환한다")
        void returnsNullForNull() {
            // when
            String result = addressHelper.extractLawdCd(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열을 입력하면 null을 반환한다")
        void returnsNullForBlank() {
            // when
            String result = addressHelper.extractLawdCd("   ");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("매칭되는 법정동코드가 없으면 null을 반환한다")
        void returnsNullWhenNotFound() {
            // given
            String address = "서울특별시 강남구 역삼동 123";
            given(regionCodeQueryPort.findLawdCd(anyString(), anyString()))
                    .willReturn(Optional.empty());
            given(regionCodeQueryPort.findLawdCdByContaining(anyString()))
                    .willReturn(Optional.empty());

            // when
            String result = addressHelper.extractLawdCd(address);

            // then
            assertThat(result).isNull();
        }
    }

    private RealTransaction createTransaction(String dongName) {
        return RealTransaction.builder()
                .dongName(dongName)
                .build();
    }
}

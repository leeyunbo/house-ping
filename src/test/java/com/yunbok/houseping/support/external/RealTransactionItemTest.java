package com.yunbok.houseping.support.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RealTransactionItem - 실거래 항목 DTO")
class RealTransactionItemTest {

    @Nested
    @DisplayName("getDealAmountAsLong() - 거래금액 Long 변환")
    class GetDealAmountAsLong {

        @Test
        @DisplayName("콤마가 있는 금액을 Long으로 변환한다")
        void parsesAmountWithCommas() {
            // given
            RealTransactionItem item = new RealTransactionItem();
            setField(item, "dealAmount", "100,000");

            // when
            Long result = item.getDealAmountAsLong();

            // then
            assertThat(result).isEqualTo(100000L);
        }

        @Test
        @DisplayName("공백이 있는 금액을 처리한다")
        void parsesAmountWithSpaces() {
            // given
            RealTransactionItem item = new RealTransactionItem();
            setField(item, "dealAmount", " 50,000 ");

            // when
            Long result = item.getDealAmountAsLong();

            // then
            assertThat(result).isEqualTo(50000L);
        }

        @Test
        @DisplayName("null 금액은 null을 반환한다")
        void returnsNullForNullAmount() {
            // given
            RealTransactionItem item = new RealTransactionItem();

            // when
            Long result = item.getDealAmountAsLong();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열은 null을 반환한다")
        void returnsNullForEmptyAmount() {
            // given
            RealTransactionItem item = new RealTransactionItem();
            setField(item, "dealAmount", "   ");

            // when
            Long result = item.getDealAmountAsLong();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("잘못된 형식은 null을 반환한다")
        void returnsNullForInvalidFormat() {
            // given
            RealTransactionItem item = new RealTransactionItem();
            setField(item, "dealAmount", "invalid");

            // when
            Long result = item.getDealAmountAsLong();

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드가 정상적으로 반환된다")
        void allFieldsReturnCorrectly() {
            // given
            RealTransactionItem item = new RealTransactionItem();
            setField(item, "aptName", "테스트아파트");
            setField(item, "dealYear", 2025);
            setField(item, "dealMonth", 1);
            setField(item, "dealDay", 15);
            setField(item, "excluUseAr", BigDecimal.valueOf(84.5));
            setField(item, "floor", 10);
            setField(item, "buildYear", 2020);
            setField(item, "umdNm", "역삼동");
            setField(item, "jibun", "123-45");

            // then
            assertThat(item.getAptName()).isEqualTo("테스트아파트");
            assertThat(item.getDealYear()).isEqualTo(2025);
            assertThat(item.getDealMonth()).isEqualTo(1);
            assertThat(item.getDealDay()).isEqualTo(15);
            assertThat(item.getExcluUseAr()).isEqualTo(BigDecimal.valueOf(84.5));
            assertThat(item.getFloor()).isEqualTo(10);
            assertThat(item.getBuildYear()).isEqualTo(2020);
            assertThat(item.getUmdNm()).isEqualTo("역삼동");
            assertThat(item.getJibun()).isEqualTo("123-45");
        }
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

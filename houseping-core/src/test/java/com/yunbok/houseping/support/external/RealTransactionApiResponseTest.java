package com.yunbok.houseping.support.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RealTransactionApiResponse - 실거래가 API 응답 DTO")
class RealTransactionApiResponseTest {

    @Nested
    @DisplayName("isSuccess() - 성공 여부 확인")
    class IsSuccess {

        @Test
        @DisplayName("resultCode가 000이면 true를 반환한다")
        void returnsTrueWhenResultCodeIs000() {
            // given
            RealTransactionApiResponse response = createResponse("000", "정상");

            // when
            boolean result = response.isSuccess();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("resultCode가 000이 아니면 false를 반환한다")
        void returnsFalseWhenResultCodeIsNot000() {
            // given
            RealTransactionApiResponse response = createResponse("99", "에러");

            // when
            boolean result = response.isSuccess();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("header가 null이면 false를 반환한다")
        void returnsFalseWhenHeaderIsNull() {
            // given
            RealTransactionApiResponse response = new RealTransactionApiResponse();

            // when
            boolean result = response.isSuccess();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getItems() - 항목 목록 조회")
    class GetItems {

        @Test
        @DisplayName("body가 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenBodyIsNull() {
            // given
            RealTransactionApiResponse response = new RealTransactionApiResponse();

            // when
            List<RealTransactionItem> items = response.getItems();

            // then
            assertThat(items).isEmpty();
        }

        @Test
        @DisplayName("items가 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenItemsIsNull() {
            // given
            RealTransactionApiResponse response = createResponseWithNullItems();

            // when
            List<RealTransactionItem> items = response.getItems();

            // then
            assertThat(items).isEmpty();
        }
    }

    private RealTransactionApiResponse createResponse(String resultCode, String resultMsg) {
        RealTransactionApiResponse response = new RealTransactionApiResponse();
        RealTransactionApiResponse.Header header = new RealTransactionApiResponse.Header();
        setField(header, "resultCode", resultCode);
        setField(header, "resultMsg", resultMsg);
        setField(response, "header", header);
        return response;
    }

    private RealTransactionApiResponse createResponseWithNullItems() {
        RealTransactionApiResponse response = new RealTransactionApiResponse();
        RealTransactionApiResponse.Body body = new RealTransactionApiResponse.Body();
        setField(response, "body", body);
        return response;
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

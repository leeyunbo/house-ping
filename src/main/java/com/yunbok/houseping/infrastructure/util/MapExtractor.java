package com.yunbok.houseping.infrastructure.util;

import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * Map에서 안전하게 값을 추출하는 유틸리티 클래스
 */
@UtilityClass
public class MapExtractor {

    /**
     * Map에서 String 값 추출 (null, 빈 문자열, "-" 처리)
     */
    public static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() || "-".equals(str) ? null : str;
    }

    /**
     * Map에서 String 값 추출 (null이면 빈 문자열 반환)
     */
    public static String getStringOrEmpty(Map<String, Object> map, String key) {
        String value = getString(map, key);
        return value != null ? value : "";
    }

    /**
     * Map에서 Integer 값 추출
     */
    public static Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Map에서 Integer 값 추출 (null이면 기본값 반환)
     */
    public static int getIntegerOrDefault(Map<String, Object> map, String key, int defaultValue) {
        Integer value = getInteger(map, key);
        return value != null ? value : defaultValue;
    }
}

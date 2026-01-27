package com.yunbok.houseping.domain.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지역명 정규화.
 * 외부 API에서 "서울" / "서울특별시", "경기" / "경기도" 등 다양한 형태로 들어오는 지역명을
 * 대표 이름 하나로 통일한다.
 */
public final class AreaNormalizer {

    private AreaNormalizer() {}

    /**
     * 같은 지역을 가리키는 이름 그룹.
     * 첫 번째 값이 대표 이름(canonical).
     */
    private static final Map<String, List<String>> ALIAS_MAP = new LinkedHashMap<>();

    static {
        ALIAS_MAP.put("서울", List.of("서울", "서울특별시"));
        ALIAS_MAP.put("경기", List.of("경기", "경기도"));
        ALIAS_MAP.put("인천", List.of("인천", "인천광역시"));
        ALIAS_MAP.put("부산", List.of("부산", "부산광역시"));
        ALIAS_MAP.put("대구", List.of("대구", "대구광역시"));
        ALIAS_MAP.put("대전", List.of("대전", "대전광역시"));
        ALIAS_MAP.put("광주", List.of("광주", "광주광역시"));
        ALIAS_MAP.put("울산", List.of("울산", "울산광역시"));
        ALIAS_MAP.put("세종", List.of("세종", "세종특별자치시"));
        ALIAS_MAP.put("강원", List.of("강원", "강원도", "강원특별자치도"));
        ALIAS_MAP.put("충북", List.of("충북", "충청북도"));
        ALIAS_MAP.put("충남", List.of("충남", "충청남도"));
        ALIAS_MAP.put("전북", List.of("전북", "전라북도", "전북특별자치도"));
        ALIAS_MAP.put("전남", List.of("전남", "전라남도"));
        ALIAS_MAP.put("경북", List.of("경북", "경상북도"));
        ALIAS_MAP.put("경남", List.of("경남", "경상남도"));
        ALIAS_MAP.put("제주", List.of("제주", "제주도", "제주특별자치도"));
    }

    /**
     * 지역명을 대표 이름으로 변환한다.
     * 매핑에 없으면 원본을 그대로 반환한다.
     */
    public static String normalize(String area) {
        if (area == null) return null;
        String trimmed = area.trim();
        for (Map.Entry<String, List<String>> entry : ALIAS_MAP.entrySet()) {
            if (entry.getValue().contains(trimmed)) {
                return entry.getKey();
            }
        }
        return trimmed;
    }

    /**
     * 대표 이름에 해당하는 모든 별칭(원본 포함)을 반환한다.
     * 검색 시 DB에 어떤 형태로 저장돼 있든 모두 매칭하기 위해 사용한다.
     */
    public static List<String> expand(String area) {
        if (area == null) return List.of();
        String canonical = normalize(area);
        List<String> aliases = ALIAS_MAP.get(canonical);
        return aliases != null ? aliases : List.of(canonical);
    }
}

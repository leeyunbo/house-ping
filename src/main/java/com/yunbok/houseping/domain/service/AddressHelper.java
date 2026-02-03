package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.RealTransaction;
import com.yunbok.houseping.domain.port.out.RegionCodeQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 주소 파싱, 동 이름 추출 및 필터링 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddressHelper {

    private final RegionCodeQueryPort regionCodeQueryPort;

    // 시도 + 시군구 패턴 (용인시 수지구 같은 복합 구조도 지원)
    private static final Pattern SIGUNGU_PATTERN = Pattern.compile(
            "(서울특별시|부산광역시|대구광역시|인천광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|" +
            "경기도|강원도|충청북도|충청남도|전라북도|전라남도|경상북도|경상남도|제주특별자치도)" +
            "\\s+([가-힣]+시)\\s+([가-힣]+구)");

    private static final Pattern SIMPLE_SIGUNGU_PATTERN = Pattern.compile(
            "(서울특별시|부산광역시|대구광역시|인천광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|" +
            "경기도|강원도|충청북도|충청남도|전라북도|전라남도|경상북도|경상남도|제주특별자치도)" +
            "\\s+([가-힣]+[시군구])");

    // 동 이름 추출 패턴: 괄호 안 또는 일반 주소에서 "XX동" 추출
    private static final Pattern DONG_IN_PAREN_PATTERN = Pattern.compile("\\(.*?([가-힣]+동)\\)");
    private static final Pattern DONG_PATTERN = Pattern.compile("([가-힣]+[0-9]*동)(?:\\s|$)");

    /**
     * 주소에서 법정동코드 추출
     */
    public String extractLawdCd(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 1차: 복합 시군구 패턴 (용인시 수지구 → 용인시수지구)
        Matcher complexMatcher = SIGUNGU_PATTERN.matcher(address);
        if (complexMatcher.find()) {
            String siName = complexMatcher.group(2);
            String guName = complexMatcher.group(3);
            String sigunguName = siName + guName;

            Optional<String> lawdCd = regionCodeQueryPort.findLawdCdByContaining(sigunguName);
            if (lawdCd.isPresent()) {
                log.debug("복합 시군구 매칭: {} → {}", sigunguName, lawdCd.get());
                return lawdCd.get();
            }
        }

        // 2차: 단순 시군구 패턴 (강남구, 수원시 등)
        Matcher simpleMatcher = SIMPLE_SIGUNGU_PATTERN.matcher(address);
        if (simpleMatcher.find()) {
            String sidoName = simpleMatcher.group(1);
            String sigunguName = simpleMatcher.group(2);

            Optional<String> lawdCd = regionCodeQueryPort.findLawdCd(sidoName, sigunguName);
            if (lawdCd.isPresent()) {
                return lawdCd.get();
            }

            // 부분 일치로 재시도
            lawdCd = regionCodeQueryPort.findLawdCdByContaining(sigunguName);
            if (lawdCd.isPresent()) {
                return lawdCd.get();
            }
        }

        log.debug("법정동코드를 찾을 수 없음: {}", address);
        return null;
    }

    /**
     * 주소에서 동 이름 추출
     */
    public String extractDongName(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 1차: 괄호 안에서 동 찾기 (더 정확함)
        Matcher parenMatcher = DONG_IN_PAREN_PATTERN.matcher(address);
        if (parenMatcher.find()) {
            return parenMatcher.group(1);
        }

        // 2차: 일반 주소에서 동 찾기
        Matcher dongMatcher = DONG_PATTERN.matcher(address);
        if (dongMatcher.find()) {
            return dongMatcher.group(1);
        }

        return null;
    }

    /**
     * 동 이름 정규화 (숫자 제거)
     */
    public String normalizeDongName(String dongName) {
        if (dongName == null) return null;
        return dongName.replaceAll("[0-9]+", "");
    }

    /**
     * 동 이름으로 거래 내역 필터링 (정규화 적용)
     */
    public List<RealTransaction> filterByDongName(List<RealTransaction> transactions, String dongName) {
        if (dongName == null || transactions.isEmpty()) {
            return transactions;
        }

        String normalizedDongName = normalizeDongName(dongName);
        return transactions.stream()
                .filter(t -> t.getDongName() != null && normalizeDongName(t.getDongName()).equals(normalizedDongName))
                .toList();
    }
}

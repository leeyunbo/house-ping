package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.infrastructure.persistence.RegionCodeEntity;
import com.yunbok.houseping.infrastructure.persistence.RegionCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 법정동코드 조회 서비스
 * 청약 주소에서 시군구 법정동코드(5자리)를 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionCodeService {

    private final RegionCodeRepository regionCodeRepository;

    /**
     * 주소에서 법정동코드 조회
     * @param address 전체 주소 (예: "경기도 안양시 만안구 안양2동 841-5번지 일대")
     * @return 법정동코드 5자리 (예: "41171")
     */
    public Optional<String> findLawdCdByAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        // 주소에서 시도, 시군구 추출
        String[] parts = address.split(" ");
        if (parts.length < 2) {
            return Optional.empty();
        }

        String sido = parts[0];
        String sigungu = parts.length >= 3 ? parts[1] + " " + parts[2] : parts[1];

        // 1차 시도: 시도 + 시군구로 정확히 조회
        Optional<RegionCodeEntity> result = findByAddress(sido, sigungu);
        if (result.isPresent()) {
            return Optional.of(result.get().getLawdCd());
        }

        // 2차 시도: 시군구만으로 조회 (예: "안양시 만안구")
        if (parts.length >= 3) {
            sigungu = parts[1] + " " + parts[2];
            List<RegionCodeEntity> matches = regionCodeRepository.findBySigunguNameContaining(sigungu);
            if (!matches.isEmpty()) {
                return Optional.of(matches.get(0).getLawdCd());
            }
        }

        // 3차 시도: 시군구명 첫 부분만으로 조회 (예: "안양시")
        sigungu = parts[1];
        List<RegionCodeEntity> matches = regionCodeRepository.findBySigunguNameContaining(sigungu);
        if (!matches.isEmpty()) {
            // 시도가 같은 것 우선
            for (RegionCodeEntity match : matches) {
                if (match.getSidoName().contains(normalizeSido(sido))) {
                    return Optional.of(match.getLawdCd());
                }
            }
            return Optional.of(matches.get(0).getLawdCd());
        }

        log.warn("법정동코드를 찾을 수 없음: {}", address);
        return Optional.empty();
    }

    private Optional<RegionCodeEntity> findByAddress(String sido, String sigungu) {
        String normalizedSido = normalizeSido(sido);

        // 정확한 매칭 시도
        Optional<RegionCodeEntity> exact = regionCodeRepository.findBySidoNameAndSigunguName(normalizedSido, sigungu);
        if (exact.isPresent()) {
            return exact;
        }

        // 시군구에 구가 포함된 경우 (예: "수원시 장안구")
        List<RegionCodeEntity> matches = regionCodeRepository.findBySigunguNameContaining(sigungu);
        for (RegionCodeEntity match : matches) {
            if (match.getSidoName().equals(normalizedSido)) {
                return Optional.of(match);
            }
        }

        return Optional.empty();
    }

    /**
     * 시도명 정규화
     * "경기" -> "경기도", "서울" -> "서울특별시" 등
     */
    private String normalizeSido(String sido) {
        if (sido == null) return "";

        return switch (sido) {
            case "서울" -> "서울특별시";
            case "부산" -> "부산광역시";
            case "대구" -> "대구광역시";
            case "인천" -> "인천광역시";
            case "광주" -> "광주광역시";
            case "대전" -> "대전광역시";
            case "울산" -> "울산광역시";
            case "세종" -> "세종특별자치시";
            case "경기" -> "경기도";
            case "강원" -> "강원특별자치도";
            case "충북" -> "충청북도";
            case "충남" -> "충청남도";
            case "전북" -> "전북특별자치도";
            case "전남" -> "전라남도";
            case "경북" -> "경상북도";
            case "경남" -> "경상남도";
            case "제주" -> "제주특별자치도";
            default -> sido;
        };
    }
}

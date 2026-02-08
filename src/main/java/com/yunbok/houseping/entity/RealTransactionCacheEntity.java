package com.yunbok.houseping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 아파트 실거래가 캐시 엔티티
 * 국토교통부 API 조회 결과를 캐싱
 */
@Entity
@Table(name = "real_transaction_cache",
       indexes = {
           @Index(name = "idx_lawd_cd_deal_ymd", columnList = "lawd_cd, deal_ymd"),
           @Index(name = "idx_apt_name", columnList = "apt_name"),
           @Index(name = "idx_deal_date", columnList = "deal_date"),
           @Index(name = "idx_cached_at", columnList = "cached_at")
       })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RealTransactionCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 법정동코드 5자리
     */
    @Column(name = "lawd_cd", nullable = false, length = 5)
    private String lawdCd;

    /**
     * 계약월 (YYYYMM)
     */
    @Column(name = "deal_ymd", nullable = false, length = 6)
    private String dealYmd;

    /**
     * 아파트명
     */
    @Column(name = "apt_name", nullable = false, length = 200)
    private String aptName;

    /**
     * 거래금액 (만원)
     */
    @Column(name = "deal_amount", nullable = false)
    private Long dealAmount;

    /**
     * 전용면적 (㎡)
     */
    @Column(name = "exclu_use_ar", precision = 10, scale = 4)
    private BigDecimal excluUseAr;

    /**
     * 층
     */
    private Integer floor;

    /**
     * 건축년도
     */
    @Column(name = "build_year")
    private Integer buildYear;

    /**
     * 거래일
     */
    @Column(name = "deal_date")
    private LocalDate dealDate;

    /**
     * 거래일 (일)
     */
    @Column(name = "deal_day")
    private Integer dealDay;

    /**
     * 법정동
     */
    @Column(name = "umd_nm", length = 100)
    private String umdNm;

    /**
     * 지번
     */
    @Column(name = "jibun", length = 50)
    private String jibun;

    /**
     * 캐시 저장 시점
     */
    @CreatedDate
    @Column(name = "cached_at", nullable = false, updatable = false)
    private LocalDateTime cachedAt;

    /**
     * 평당 가격 계산 (만원/평)
     */
    public Long getPricePerPyeong() {
        if (excluUseAr == null || excluUseAr.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        // 1평 = 3.3058㎡
        BigDecimal pyeong = excluUseAr.divide(BigDecimal.valueOf(3.3058), 2, java.math.RoundingMode.HALF_UP);
        return BigDecimal.valueOf(dealAmount).divide(pyeong, 0, java.math.RoundingMode.HALF_UP).longValue();
    }
}

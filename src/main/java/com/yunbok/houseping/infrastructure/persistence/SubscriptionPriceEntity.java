package com.yunbok.houseping.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 청약 분양가 상세 정보 엔티티
 * 주택형별 분양가 데이터를 저장
 */
@Entity
@Table(name = "subscription_price",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_subscription_price",
           columnNames = {"house_manage_no", "pblanc_no", "model_no"}
       ),
       indexes = {
           @Index(name = "idx_price_house_manage_no", columnList = "house_manage_no"),
           @Index(name = "idx_price_pblanc_no", columnList = "pblanc_no")
       })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주택관리번호
     */
    @Column(name = "house_manage_no", nullable = false, length = 40)
    private String houseManageNo;

    /**
     * 공고번호
     */
    @Column(name = "pblanc_no", nullable = false, length = 40)
    private String pblancNo;

    /**
     * 모델번호
     */
    @Column(name = "model_no", nullable = false, length = 20)
    private String modelNo;

    /**
     * 주택형 (예: 59A, 84B)
     */
    @Column(name = "house_type", length = 50)
    private String houseType;

    /**
     * 주택공급면적 (제곱미터)
     */
    @Column(name = "supply_area", precision = 10, scale = 2)
    private BigDecimal supplyArea;

    /**
     * 일반공급세대수
     */
    @Column(name = "supply_count")
    private Integer supplyCount;

    /**
     * 특별공급세대수
     */
    @Column(name = "special_supply_count")
    private Integer specialSupplyCount;

    /**
     * 분양최고금액 (만원)
     */
    @Column(name = "top_amount")
    private Long topAmount;

    /**
     * 평당 가격 (만원/평)
     */
    @Column(name = "price_per_pyeong")
    private Long pricePerPyeong;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 평당 가격 계산
     */
    public void calculatePricePerPyeong() {
        if (topAmount == null || supplyArea == null || supplyArea.compareTo(BigDecimal.ZERO) == 0) {
            this.pricePerPyeong = null;
            return;
        }
        // 1평 = 3.3058 제곱미터
        BigDecimal pyeong = supplyArea.divide(BigDecimal.valueOf(3.3058), 2, java.math.RoundingMode.HALF_UP);
        if (pyeong.compareTo(BigDecimal.ZERO) == 0) {
            this.pricePerPyeong = null;
            return;
        }
        this.pricePerPyeong = BigDecimal.valueOf(topAmount).divide(pyeong, 0, java.math.RoundingMode.HALF_UP).longValue();
    }
}

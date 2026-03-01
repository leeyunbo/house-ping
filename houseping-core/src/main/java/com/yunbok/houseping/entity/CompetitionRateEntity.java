package com.yunbok.houseping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 청약 경쟁률 엔티티
 * 당첨자 발표일 이후 수집된 경쟁률 데이터
 */
@Entity
@Table(name = "competition_rate",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_competition_rate",
                columnNames = {"house_manage_no", "pblanc_no", "house_type", "residence_area", "rank"}
        ),
        indexes = {
                @Index(name = "idx_competition_rate_house", columnList = "house_manage_no, pblanc_no")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CompetitionRateEntity {

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
     * 주택형 (084.9543T)
     */
    @Column(name = "house_type", length = 20)
    private String houseType;

    /**
     * 공급세대수
     */
    @Column(name = "supply_count")
    private Integer supplyCount;

    /**
     * 접수건수
     */
    @Column(name = "request_count")
    private Integer requestCount;

    /**
     * 경쟁률
     */
    @Column(name = "competition_rate", precision = 10, scale = 2)
    private BigDecimal competitionRate;

    /**
     * 거주지역 (해당지역/기타지역)
     */
    @Column(name = "residence_area", length = 50)
    private String residenceArea;

    /**
     * 순위 (1순위/2순위)
     */
    @Column(name = "rank")
    private Integer rank;

    /**
     * 청약 정보 (읽기 전용 네비게이션)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_manage_no", referencedColumnName = "house_manage_no",
                insertable = false, updatable = false)
    private SubscriptionEntity subscription;

    /**
     * 수집 일시
     */
    @CreatedDate
    @Column(name = "collected_at", nullable = false, updatable = false)
    private LocalDateTime collectedAt;

    public BigDecimal getEffectiveRate() {
        if (competitionRate != null) {
            return competitionRate;
        }
        if (supplyCount != null && supplyCount > 0 && requestCount != null) {
            return BigDecimal.valueOf(requestCount)
                    .divide(BigDecimal.valueOf(supplyCount), 2, RoundingMode.HALF_UP);
        }
        return null;
    }
}

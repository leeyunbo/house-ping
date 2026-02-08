package com.yunbok.houseping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 청약 정보 엔티티
 * API에서 수집한 청약 데이터를 DB에 저장
 */
@Entity
@Table(name = "subscription_info",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_subscription",
           columnNames = {"source", "house_name", "receipt_start_date"}
       ),
       indexes = {
           @Index(name = "idx_receipt_start_date", columnList = "receipt_start_date"),
           @Index(name = "idx_area", columnList = "area"),
           @Index(name = "idx_source", columnList = "source")
       })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 데이터 소스 (LH, ApplyHome)
     */
    @Column(nullable = false, length = 50)
    private String source;

    /**
     * 주택관리번호 (청약홈 API용)
     */
    @Column(name = "house_manage_no", length = 40)
    private String houseManageNo;

    /**
     * 공고번호 (청약홈 API용)
     */
    @Column(name = "pblanc_no", length = 40)
    private String pblancNo;

    /**
     * 주택명
     */
    @Column(nullable = false, length = 200)
    private String houseName;

    /**
     * 주택 유형 (APT, 무순위, 신혼희망타운 등)
     */
    @Column(length = 50)
    private String houseType;

    /**
     * 지역 (서울, 경기 등)
     */
    @Column(length = 50)
    private String area;

    /**
     * 공고일
     */
    private LocalDate announceDate;

    /**
     * 접수 시작일
     */
    @Column(nullable = false)
    private LocalDate receiptStartDate;

    /**
     * 접수 종료일
     */
    private LocalDate receiptEndDate;

    /**
     * 당첨자 발표일
     */
    private LocalDate winnerAnnounceDate;

    /**
     * 상세 URL
     */
    @Column(length = 500)
    private String detailUrl;

    /**
     * 홈페이지 URL
     */
    @Column(length = 500)
    private String homepageUrl;

    /**
     * 연락처
     */
    @Column(length = 50)
    private String contact;

    /**
     * 총 공급 세대수
     */
    private Integer totalSupplyCount;

    /**
     * 공급 위치 주소
     */
    @Column(length = 500)
    private String address;

    /**
     * 우편번호
     */
    @Column(length = 10)
    private String zipCode;

    /**
     * 수집 일시
     */
    @Column(nullable = false)
    private LocalDateTime collectedAt;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (collectedAt == null) {
            collectedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        collectedAt = LocalDateTime.now();
    }

    /**
     * 업데이트가 필요한지 확인
     */
    public boolean needsUpdate(SubscriptionEntity other) {
        if (other == null) return false;

        return !equals(houseManageNo, other.houseManageNo)
            || !equals(pblancNo, other.pblancNo)
            || !equals(receiptStartDate, other.receiptStartDate)
            || !equals(receiptEndDate, other.receiptEndDate)
            || !equals(winnerAnnounceDate, other.winnerAnnounceDate)
            || !equals(detailUrl, other.detailUrl)
            || !equals(totalSupplyCount, other.totalSupplyCount)
            || !equals(address, other.address)
            || !equals(zipCode, other.zipCode);
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * 다른 엔티티의 값으로 업데이트
     */
    public void updateFrom(SubscriptionEntity other) {
        this.houseManageNo = other.houseManageNo;
        this.pblancNo = other.pblancNo;
        this.houseType = other.houseType;
        this.area = other.area;
        this.announceDate = other.announceDate;
        this.receiptStartDate = other.receiptStartDate;
        this.receiptEndDate = other.receiptEndDate;
        this.winnerAnnounceDate = other.winnerAnnounceDate;
        this.detailUrl = other.detailUrl;
        this.homepageUrl = other.homepageUrl;
        this.contact = other.contact;
        this.totalSupplyCount = other.totalSupplyCount;
        this.address = other.address;
        this.zipCode = other.zipCode;
    }
}

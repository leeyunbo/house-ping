package com.yunbok.houseping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 법정동코드 매핑 엔티티
 * 시군구명 → 법정동코드(5자리) 매핑
 */
@Entity
@Table(name = "region_code",
       indexes = {
           @Index(name = "idx_region_name", columnList = "region_name")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_lawd_cd", columnNames = "lawd_cd")
       })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RegionCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 시도명 (서울특별시, 경기도 등)
     */
    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName;

    /**
     * 시군구명 (강남구, 수원시 등)
     */
    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    /**
     * 전체 지역명 (서울특별시 강남구)
     */
    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    /**
     * 법정동코드 5자리 (11680)
     */
    @Column(name = "lawd_cd", nullable = false, length = 5)
    private String lawdCd;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

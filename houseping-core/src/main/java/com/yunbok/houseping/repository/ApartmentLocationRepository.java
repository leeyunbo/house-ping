package com.yunbok.houseping.repository;

import com.yunbok.houseping.entity.ApartmentLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentLocationRepository extends JpaRepository<ApartmentLocationEntity, Long> {

    Optional<ApartmentLocationEntity> findByAptNameAndLawdCd(String aptName, String lawdCd);

    List<ApartmentLocationEntity> findByLawdCd(String lawdCd);

    boolean existsByAptNameAndLawdCd(String aptName, String lawdCd);
}

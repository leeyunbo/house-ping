package com.yunbok.houseping.persistence;

import com.yunbok.houseping.core.port.ApartmentLocationPort;
import com.yunbok.houseping.entity.ApartmentLocationEntity;
import com.yunbok.houseping.repository.ApartmentLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApartmentLocationStore implements ApartmentLocationPort {

    private final ApartmentLocationRepository repository;

    @Override
    public Optional<double[]> findLocation(String aptName, String lawdCd) {
        return repository.findByAptNameAndLawdCd(aptName, lawdCd)
                .map(e -> new double[]{e.getLatitude(), e.getLongitude()});
    }

    @Override
    public Map<String, double[]> findLocationsByLawdCd(String lawdCd) {
        return repository.findByLawdCd(lawdCd).stream()
                .collect(Collectors.toMap(
                        ApartmentLocationEntity::getAptName,
                        e -> new double[]{e.getLatitude(), e.getLongitude()},
                        (a, b) -> a
                ));
    }

    @Override
    public boolean exists(String aptName, String lawdCd) {
        return repository.existsByAptNameAndLawdCd(aptName, lawdCd);
    }

    @Override
    public void save(String aptName, String lawdCd, String dongName, String jibun, double latitude, double longitude) {
        ApartmentLocationEntity entity = ApartmentLocationEntity.builder()
                .aptName(aptName)
                .lawdCd(lawdCd)
                .dongName(dongName)
                .jibun(jibun)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        repository.save(entity);
    }
}

package com.artigile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.Collection;
import java.util.Date;

@RepositoryRestResource
public interface SensorRepository extends JpaRepository<SensorData, Date> {
    @RestResource(path = "byRange")
    @Query("from SensorData s where s.id > :startDate and s.id < :endDate")
    Collection<SensorData> findById(@Param("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
                                    @Param("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate);
}

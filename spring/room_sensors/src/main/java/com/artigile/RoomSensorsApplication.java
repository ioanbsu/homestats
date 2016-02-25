package com.artigile;

import com.artigile.homestats.model.SensorData;
import com.artigile.homestats.sensor.BMP085AnfDht11;
import com.artigile.homestats.sensor.DummySensor;
import com.artigile.homestats.sensor.HTU21F;
import com.artigile.homestats.sensor.SensorsDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;

@EnableDiscoveryClient
@SpringBootApplication
public class RoomSensorsApplication {

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurerAdapter() {
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
                config.exposeIdsFor(SensorData.class);
            }

        };
    }

    @Bean
    @Profile("default")
    public SensorsDataProvider dummyDataProvider() {
        return new DummySensor();
    }

    @Bean
    @Profile("htu21f")
    public SensorsDataProvider htu21DataProvider() {
        return new HTU21F();
    }

    @Bean
    @Profile("bmp085anfdht11")
    public SensorsDataProvider bmp085DataProvider() {
        try {
            return new BMP085AnfDht11();
        } catch (IOException e) {
            return null;
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(RoomSensorsApplication.class, args);
    }


}

@RefreshScope
@RestController
class SensorsRestController {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorsDataProvider sensorsDataProvider;

    @Value("${message}")
    private String msg;

    @RequestMapping("/sensors")
    Collection<SensorData> getSensoersDataForDates(@RequestParam("startDate") @DateTimeFormat(pattern = SensorData.DATE_TIME_FORMAT) Date startDate,
                                                   @RequestParam("endDate") @DateTimeFormat(pattern = SensorData.DATE_TIME_FORMAT) Date endDate) {
        return sensorRepository.findById(startDate, endDate);
    }

    @RequestMapping("/currentTemp")
    Float getCurrentTemp() {
        try {
            return sensorsDataProvider.readTemperature();
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping("/currentHumidity")
    Float getCurrentHumidity() {
        try {
            return sensorsDataProvider.readHumidity();
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping("/currentPressure")
    Integer getCurrentPressure() {
        try {
            return sensorsDataProvider.readPressure();
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping("/recentSensors")
    Collection<SensorData> message() {
        return sensorRepository.findById(Date.from(LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC)), new Date());
    }


}

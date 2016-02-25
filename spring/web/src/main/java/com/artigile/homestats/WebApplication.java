package com.artigile.homestats;

import com.artigile.homestats.model.SensorData;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}

@RestController
@RequestMapping("/sensors")
class SensorsRestController {


    @Autowired
    private RestTemplate restTemplate;

    private SimpleDateFormat requestDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

    @HystrixCommand(fallbackMethod = "defaultDataHistory")
    @RequestMapping(method = RequestMethod.GET, value = "lastN")
    public Collection<Float[]> lastNValues() {

        SensorData[] responseArray = restTemplate.getForObject("http://ivannaroom/recentSensors", SensorData[].class);
        Collection<SensorData> responseCollection = Arrays.asList(responseArray);
        return responseCollection
                .stream()
                .map(SensorData::toDataArray)
                .collect(Collectors.toList());
    }


    @HystrixCommand(fallbackMethod = "defaultDataHistory")
    @RequestMapping(method = RequestMethod.GET, value = "byRange")
    public Collection<Float[]> getByRange() {
        String startDate = requestDateTimeFormat.format(Date.from(LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC)));
        String endDate = requestDateTimeFormat.format(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));

        SensorData[] responseArray = restTemplate.getForObject("http://ivannaroom/sensors?startDate=" + startDate
                        + "&endDate=" + endDate,
                SensorData[].class);
        Collection<SensorData> responseCollection = Arrays.asList(responseArray);
        return responseCollection
                .stream()
                .map(SensorData::toDataArray)
                .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value = "currentTemp")
    public Float getCurrentTemp() {
        return new BigDecimal(restTemplate.getForObject("http://ivannaroom/currentTemp", Float.class))
                .setScale(2, BigDecimal.ROUND_CEILING).floatValue();
    }

    @RequestMapping(method = RequestMethod.GET, value = "currentHumidity")
    public Float getCurrentHumidity() {
        return new BigDecimal(restTemplate.getForObject("http://ivannaroom/currentHumidity", Float.class))
                .setScale(2, BigDecimal.ROUND_CEILING).floatValue();
    }

    @RequestMapping(method = RequestMethod.GET, value = "currentPressure")
    public Float getCurrentPressure() {
        return new BigDecimal(restTemplate.getForObject("http://ivannaroom/currentPressure", Float.class))
                .setScale(2, BigDecimal.ROUND_CEILING).floatValue();
    }


    public Collection<Float[]> defaultDataHistory() {
        return new ArrayList<>();
    }


}
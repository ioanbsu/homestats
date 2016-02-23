package com.artigile;

import com.artigile.homestats.model.SensorData;
import com.artigile.homestats.sensor.SensorsDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ibahdanau on 2/22/16.
 */
@Component
public class SensorsPeriodicalDataSaver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorsPeriodicalDataSaver.class);


    @Autowired
    private SensorsDataProvider sensorsDataProvider;
    @Autowired
    SensorRepository sensorRepository;

    @Scheduled(fixedRate = 1000 * 30 * 5)
    public void readDataFromSensors() {
        SensorData sensorData = new SensorData();
        try {
            sensorData.setHumidity(sensorsDataProvider.readHumidity());
        } catch (Exception e) {
            sensorData.setHumidity(-1F);
        }
        try {
            sensorData.setTemperature(sensorsDataProvider.readTemperature());
        } catch (Exception e) {
            Page<SensorData> prevTempData = sensorRepository.findAll(new PageRequest(0, 1, Sort.Direction.DESC, "id"));
            if (prevTempData.getTotalElements() != 0) {
                sensorData.setTemperature(prevTempData.getContent().get(0).getTemperature());
            } else {
                sensorData.setTemperature(-99999F);
            }
        }
        Page<SensorData> prevPressureData = sensorRepository.findAll(new PageRequest(0, 1440, Sort.Direction.DESC, "id"));

        try {
            int pressure = getPressure(prevPressureData.getContent()
                    .stream()
                    .map(SensorData::getPressure)
                    .collect(Collectors.toList()));
            sensorData.setPressure(pressure);
        } catch (Exception e) {
            sensorData.setPressure(-1);
        }

        sensorData.setId(new Date());
        sensorRepository.save(sensorData);
    }


    private int getPressure(final List<Integer> pressureList) throws Exception {
        try {
            int pressure = sensorsDataProvider.readPressure();
            int count = 0;
            final int maxRetries = 5;
            final int ridiculouslyLowPressure = 85000;
            final int ridiculouslyHighPressure = 105000;
            while ((pressure > ridiculouslyHighPressure || pressure < ridiculouslyLowPressure)
                    && count++ < maxRetries) {
                Thread.sleep(1000);//sleep to prevent i2c channel overload
                LOGGER.info("Retrying to read valid pressure value.Returned too low: {}", pressure);
                pressure = sensorsDataProvider.readPressure();
            }
            if (pressure < ridiculouslyLowPressure || pressure > ridiculouslyHighPressure) {//in case retry did not help just sending ack last read pressure.
                LOGGER.warn("The pressure failed to be calculated. The ridiculously low value read. Skip save new value.");
                if (pressureList.size() == 0) {
                    throw new IllegalStateException("The pressure failed to be calculated." +
                            " The ridiculously low value read. Skip save new value.");
                }
                pressure = pressureList.get(pressureList.size() - 1);
            }
            final double movingAverage = movingAverage(pressureList);
            final double standardDeviation = standardDeviation(pressureList, movingAverage);
            LOGGER.info("Standard deviation {}, moving average: {}", standardDeviation, movingAverage);

            double delta = Math.abs(movingAverage - pressure);

            if (delta > standardDeviation * 5) {
                if (pressureList.size() == 0) {
                    throw new IllegalStateException("The pressure failed to be calculated. The ridiculously low value read. Skip save new value.");
                }
                LOGGER.warn("The calculated new pressure value is too high from " +
                                "the standard deviation. Sensor data: [{}]. Saving Last known good value {}", pressure,
                        pressureList.get(0));
                pressure = pressureList.get(0);
            }
            return pressure;
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    private double standardDeviation(final List<Integer> pressureList, final double mean) {
        if (pressureList.isEmpty()) {
            return 0;
        }
        double standardDeviation = 0.;
        for (Integer pressure : pressureList) {
            double submean = pressure - mean;
            double squared = submean * submean;
            standardDeviation += squared;
        }
        return Math.sqrt(standardDeviation / pressureList.size());
    }

    private double movingAverage(final List<Integer> pressureList) {
        if (pressureList.isEmpty()) {
            return 0;
        }
        double movingAverage = 0;
        for (Integer pressure : pressureList) {
            movingAverage += (double) pressure;
        }
        return movingAverage / pressureList.size();
    }
}


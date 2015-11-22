package com.artigile.homestats;

import com.artigile.homestats.sensor.SensorsDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ivanbahdanau
 */
public class DataService implements Runnable {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    private final ScheduledExecutorService executorService;
    private final DbDao dbDao;
    private final SensorsDataProvider sensorsDataProvider;
    private final long period;


    public DataService(final SensorsDataProvider sensorsDataProvider, final DbDao dbDao, final long period) {
        this.dbDao = dbDao;
        this.sensorsDataProvider = sensorsDataProvider;
        this.executorService = Executors.newScheduledThreadPool(1);
        this.period = period;
    }

    @Override
    public void run() {
        if (sensorsDataProvider != null) {
            try {
                int pressure = getPressure(dbDao.getPressureList());
                dbDao.saveTempAndHumidity(sensorsDataProvider.readTemperature(), sensorsDataProvider.readHumidity(), pressure);
            } catch (Exception e) {
                LOGGER.error("Failed to read temperature AND/OR humidity", e);
            }
        }
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

    public void start() {
        executorService.scheduleAtFixedRate(this, 0, period, TimeUnit.MILLISECONDS);
    }
}

package com.artigile.homestats;

import com.artigile.homestats.sensor.SensorsDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ivanbahdanau
 */
public class DataSaver implements Runnable {

    private int lastMeasured;
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSaver.class);

    private final ScheduledExecutorService executorService;

    private final DbService dbService;
    private final SensorsDataProvider sensorsDataProvider;
    private final long period;


    public DataSaver(final SensorsDataProvider sensorsDataProvider, final DbService dbService, final long period) {
        this.dbService = dbService;
        this.sensorsDataProvider = sensorsDataProvider;
        this.executorService = Executors.newScheduledThreadPool(1);
        this.period = period;
    }

    @Override
    public void run() {
        if (sensorsDataProvider != null) {
            try {
                int pressure = getPressure();
                dbService.saveTempAndHumidity(sensorsDataProvider.readTemperature(), sensorsDataProvider.readHumidity(), pressure);
            } catch (Exception e) {
                LOGGER.error("Failed to read temperature AND/OR humidity", e);
            }
        }
    }

    private int getPressure() {
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
                throw new IllegalStateException("The pressure failed to be calculated. The ridiculously low value read. Skip save.");
            }
            if (lastMeasured != 0) {
                int delta = Math.abs(lastMeasured - pressure);
                if (delta > 1000) {
                    if (pressure > lastMeasured) {
                        pressure = pressure + delta / 10;
                    } else {
                        pressure = pressure - delta / 10;
                    }
                }
            }
            lastMeasured = pressure;
            return pressure;
        } catch (Exception e) {
            LOGGER.error("Failed to read the pressure", e);
            return 0;
        }

    }

    public void start() {
        executorService.scheduleAtFixedRate(this, 0, period, TimeUnit.MILLISECONDS);
    }
}

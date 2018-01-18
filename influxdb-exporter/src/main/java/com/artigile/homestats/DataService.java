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
                dbDao.saveTempAndHumidity(sensorsDataProvider.readTemperature(), sensorsDataProvider.readHumidity(),
                    sensorsDataProvider.readPressure());
            } catch (Exception e) {
                LOGGER.error("Failed to read temperature AND/OR humidity", e);
            }
        }
    }
    public void start() {
        executorService.scheduleAtFixedRate(this, 0, period, TimeUnit.MILLISECONDS);
    }
}

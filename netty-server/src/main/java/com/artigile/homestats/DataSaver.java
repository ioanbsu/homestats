package com.artigile.homestats;

import com.artigile.homestats.sensor.HTU21F;
import com.artigile.homestats.sensor.TempAndHumidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ivanbahdanau
 */
public class DataSaver implements Runnable {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSaver.class);

    private final ScheduledExecutorService executorService;

    private final DbService dbService;
    private final TempAndHumidity tempAndHumidity;
    private final long period;


    public DataSaver(final TempAndHumidity tempAndHumidity, final DbService dbService, final long period) {
        this.dbService = dbService;
        this.tempAndHumidity = tempAndHumidity;
        this.executorService = Executors.newScheduledThreadPool(1);
        this.period = period;
    }

    @Override
    public void run() {
        if (tempAndHumidity != null) {
            try {
                dbService.saveTempAndHumidity(tempAndHumidity.readTemperature(), tempAndHumidity.readHumidity());
            } catch (Exception e) {
                LOGGER.error("Failed to read temperature AND/OR humidity", e);
            }
        }
    }

    public void start() {
        executorService.scheduleAtFixedRate(this, 0, period, TimeUnit.MILLISECONDS);
    }
}

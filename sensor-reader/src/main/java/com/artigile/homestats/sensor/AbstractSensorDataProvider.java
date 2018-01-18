package com.artigile.homestats.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSensorDataProvider implements SensorsDataProvider{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSensorDataProvider.class);

    @Override
    public void printAll() throws Exception {
        LOGGER.info("Temperature: {}. ", readTemperature());
        LOGGER.info("Humidity: {}", readHumidity());
        LOGGER.info("Pressure(in Pa) {}", readPressure());
    }
}

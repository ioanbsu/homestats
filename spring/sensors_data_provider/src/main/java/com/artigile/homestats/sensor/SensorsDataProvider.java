package com.artigile.homestats.sensor;

/**
 * Created by ibahdanau on 2/22/16.
 */
public interface SensorsDataProvider {
    float readTemperature() throws Exception;

    float readHumidity() throws Exception;

    int readPressure() throws Exception;
}

package com.artigile.homestats.sensor;

/**
 * @author ivanbahdanau
 */
public interface SensorsDataProvider {
    float readTemperature() throws Exception;

    float readHumidity() throws Exception;

    int readPressure() throws Exception;

    void printAll() throws Exception;
}

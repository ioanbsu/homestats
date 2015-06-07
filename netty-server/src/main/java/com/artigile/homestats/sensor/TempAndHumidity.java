package com.artigile.homestats.sensor;

/**
 * @author ivanbahdanau
 */
public interface TempAndHumidity {
    float readTemperature() throws Exception;

    float readHumidity() throws Exception;
}

package com.artigile.homestats.sensor;

/**
 * Created by ibahdanau on 2/22/16.
 */
public class DummySensor implements SensorsDataProvider {
    @Override
    public float readTemperature() throws Exception {
        return (float) (Math.random() * 30);
    }

    @Override
    public float readHumidity() throws Exception {
        return (float) (Math.random() * 100);
    }

    @Override
    public int readPressure() throws Exception {
        return (int) (Math.random() * 50);
    }
}

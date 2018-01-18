package com.artigile.homestats.sensor;

public class MockDataProvider extends AbstractSensorDataProvider {
    @Override
    public float readTemperature() throws Exception {
        return (float) (24 + Math.random() * 5);
    }

    @Override
    public float readHumidity() throws Exception {
        return (float) (60 + Math.random() * 5);
    }

    @Override
    public int readPressure() throws Exception {
        return (int) (100000 + Math.random() * 5);
    }

}

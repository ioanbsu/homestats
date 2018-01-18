package com.artigile.homestats.sensor;

import java.io.IOException;

public class SensorFactory {
    public static SensorsDataProvider buildSensorDataProvider(final SensorMode appMode) throws IOException {
        SensorsDataProvider sensorsDataProvider;
        if (appMode == SensorMode.HTU21F) {
            sensorsDataProvider = new HTU21F();
        } else if (appMode == SensorMode.BMP085) {
            sensorsDataProvider = new BMP085AnfDht11();
            ((BMP085AnfDht11) sensorsDataProvider).init();
        } else if(appMode==SensorMode.DEV){
            sensorsDataProvider = new MockDataProvider();
        } else {
            sensorsDataProvider = null;
        }
        return sensorsDataProvider;
    }
}

package com.artigile.homestats.sensor;

/**
 * @author ivanbahdanau
 */
public enum SensorMode {
    HTU21F,//prod mode when the app reads the data from real sensors
    BMP085,//prod mode when the app reads the data from real sensors
    DEV // dev mode - all the data is fake-ed.
    ;

}

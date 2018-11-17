package com.artigile.homestats.sensor.dyson;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for dyson device connection(mostly for demo purposes).
 *
 */
public class Dyson {

    private final static Logger LOGGER = LoggerFactory.getLogger(MDnsDysonFinder.class);

    public static void main(
        String[] args) throws UnirestException, IOException, MqttException {
        final String email = args[0];
        final String password = args[1];

        final Set<DysonDataProvider> devices = new DysonConnect(email, password).watchLocalDevices(10,
            TimeUnit.SECONDS);
        devices.forEach(dysonDataProvider -> {
            dysonDataProvider.registerDataConsumer(dysonDevice -> {
                if (dysonDevice.dysonPureCoolData.currentSensorData != null) {
                    LOGGER.info("\n\tDevice: {}. \n\tSensors data:{}", dysonDevice.deviceDescription,
                        dysonDevice.dysonPureCoolData.currentSensorData);
                }
            });
        });

    }
}

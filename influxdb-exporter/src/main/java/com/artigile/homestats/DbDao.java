package com.artigile.homestats;

import com.artigile.homestats.sensor.dyson.model.DysonDevice;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author ivanbahdanau
 */
public class DbDao {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbDao.class);
    private InfluxDB influxDB;
    private String dbName;

    public DbDao(final String dbHost, final String user, final String password, final int port,
                 final String dbName) {
        this.influxDB = InfluxDBFactory.connect("http://" + dbHost + ":" + port, user, password);
        this.dbName = dbName;
        influxDB.enableBatch(10, 10, TimeUnit.SECONDS);
    }

    /**
     * saves humidity and temperature to the DB.
     *
     * @param temperature temperature to save.
     * @param humidity humidity to save.
     */
    public void saveTempAndHumidity(float temperature, float humidity, final int pressure) {
        LOGGER.info("Trying to save, temperature: {}, humidity: {}, pressure: {}", temperature, humidity, pressure);

        final BatchPoints batchPoints = BatchPoints
            .database(dbName)
            .tag("async", "true")
            .consistency(InfluxDB.ConsistencyLevel.ALL)
            .build();
        Point temperaturePoint = Point.measurement("temperature")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("value", temperature)
            .build();
        Point humidityPoint = Point.measurement("humidity")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("value", humidity)
            .build();
        Point pressurePoint = Point.measurement("pressure")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("value", pressure)
            .build();
        batchPoints.point(temperaturePoint);
        batchPoints.point(humidityPoint);
        batchPoints.point(pressurePoint);
        influxDB.write(batchPoints);
        influxDB.flush();
    }

    public void saveDysonData(final DysonDevice dysonDevice) {
        if (dysonDevice.dysonPureCoolData.currentSensorData != null) {

            LOGGER.info("\n\tTrying to save data from Dyson Device: {}. \n\tSensors data:{}",
                dysonDevice.deviceDescription,
                dysonDevice.dysonPureCoolData.currentSensorData);

            final Point dysonPuerCoolStats = Point.measurement("DysonPureCool")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("serial", dysonDevice.deviceDescription.serial)
                .tag("name", dysonDevice.deviceDescription.name)
                .addField("tact", dysonDevice.dysonPureCoolData.currentSensorData.tact)
                .addField("hact", dysonDevice.dysonPureCoolData.currentSensorData.hact)
                .addField("pm25", dysonDevice.dysonPureCoolData.currentSensorData.pm25)
                .addField("pm10", dysonDevice.dysonPureCoolData.currentSensorData.pm10)
                .addField("va10", dysonDevice.dysonPureCoolData.currentSensorData.va10)
                .addField("noxl", dysonDevice.dysonPureCoolData.currentSensorData.noxl)
                .addField("p25r", dysonDevice.dysonPureCoolData.currentSensorData.p25r)
                .addField("p10r", dysonDevice.dysonPureCoolData.currentSensorData.p10r)
                .build();

            final BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

            batchPoints.point(dysonPuerCoolStats);
            influxDB.write(batchPoints);
            influxDB.flush();

        }
    }
}

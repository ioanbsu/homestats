package com.artigile.homestats;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
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

    public DbDao(final String dbHost, final String user, final String password, final int port, final String dbName) throws SQLException {
        this.influxDB = InfluxDBFactory.connect("http://" + dbHost + ":" + port, user, password);
        this.dbName=dbName;
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

    }
}

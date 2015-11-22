package com.artigile.homestats;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author ivanbahdanau
 */
public class DbDao {
    private Connection conn;

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbDao.class);

    public DbDao(final String dbHost, final String user, final String password) throws SQLException {
        try {
            // create our mysql database connection
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = "jdbc:mysql://" + dbHost + ":3306/ivannaroom";
            Class.forName(myDriver);
            conn = DriverManager.getConnection(myUrl, user, password);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error initiating DB connection", e);
        }
    }

    public String getSerializedStats() {
        try (Statement st = conn.createStatement()) {
            String query = "SELECT UNIX_TIMESTAMP(id),temperature, humidity, pressure FROM sensor_stats WHERE id > " +
              "DATE_ADD(CURDATE(),INTERVAL -2 DAY) ORDER BY id ASC";
            ResultSet rs = st.executeQuery(query);
            // iterate through the java resultset
            StringBuilder responseBuilder = new StringBuilder();
            while (rs.next()) {
                final String timestamp = rs.getString(1);
                final Float temp = rs.getFloat("temperature");
                final Float humidity = rs.getFloat("humidity");
                final Integer pressure = rs.getInt("pressure");

                responseBuilder.append("[")
                  .append(timestamp)
                  .append(", ")
                  .append(temp)
                  .append(", ")
                  .append(humidity)
                  .append(", ")
                  .append(pressure)
                  .append("], ");

            }
            String data = responseBuilder.toString();
            data = data.substring(0, data.lastIndexOf(","));
            data = "[" + data + "]";
            st.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * saves humidity and temperature to the DB.
     *
     * @param temperature temperature to save.
     * @param humidity humidity to save.
     */
    public void saveTempAndHumidity(float temperature, float humidity, final int pressure) {
        try (Statement st = conn.createStatement()) {
            String query =
              "insert into sensor_stats (id,temperature,humidity,pressure) values(now()," + temperature + "," +
                humidity + "," + pressure + ")";
            int rowChanged = st.executeUpdate(query);
            if (rowChanged == 1) {
                LOGGER.info("Saved, temperature: {}, humidity: {}, pressure: {}", temperature, humidity, pressure);
            } else {
                LOGGER.warn("No data seems to be saved to the DB. Temperature: {}, humidity: {}, pressure: {}.",
                  temperature, humidity, pressure);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public List<Integer> getPressureList() {
        try (Statement st = conn.createStatement()) {
            String query = "SELECT pressure FROM sensor_stats ORDER BY id DESC LIMIT 1440";
            ResultSet rs = st.executeQuery(query);
            ImmutableList.Builder<Integer> pressureListBuilder = ImmutableList.builder();
            while (rs.next()) {
                pressureListBuilder.add(rs.getInt("pressure"));
            }
            return pressureListBuilder.build();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ImmutableList.of();
    }
}

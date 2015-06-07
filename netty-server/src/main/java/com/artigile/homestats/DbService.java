package com.artigile.homestats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author ivanbahdanau
 */
public class DbService {
    private Connection conn;

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbService.class);


    public DbService(final String dbHost, final String user, final String password) throws SQLException {
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
        Statement st = null;
        try {
            String query = "SELECT UNIX_TIMESTAMP(id),temperature, humidity FROM sensor_stats WHERE id > DATE_ADD(CURDATE(),INTERVAL -2 DAY) ORDER BY id ASC";
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            // iterate through the java resultset
            StringBuilder responseBuilder = new StringBuilder();
            while (rs.next()) {
                final String timestamp = rs.getString(1);
                final Float temp = rs.getFloat("temperature");
                final Float humidity = rs.getFloat("humidity");

                responseBuilder.append("[")
                        .append(timestamp)
                        .append(", ")
                        .append(temp)
                        .append(", ")
                        .append(humidity)
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
     * @param humidity    humidity to save.
     */
    public void saveTempAndHumidity(float temperature, float humidity) {
        try {
            String query = "insert into sensor_stats (id,temperature,humidity) values(now()," + temperature + "," + humidity + ")";
            Statement st = conn.createStatement();
            int rowChanged = st.executeUpdate(query);
            if (rowChanged == 1) {
                LOGGER.info("Saved, temperature: " + temperature + ", humidity: " + humidity);
            } else {
                LOGGER.warn("No data seems to be saved to the DB. Temperature: " + temperature + ", humidity: " + humidity);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
}

package com.artigile.homestats.exporter;

import static com.artigile.homestats.exporter.ArgsParser.APP_PORT_OPTION;
import static com.artigile.homestats.exporter.ArgsParser.DB_HOST_OPTION;
import static com.artigile.homestats.exporter.ArgsParser.DB_NAME;
import static com.artigile.homestats.exporter.ArgsParser.DB_PWD_OPTION;
import static com.artigile.homestats.exporter.ArgsParser.DB_USER_OPTION;
import static com.artigile.homestats.exporter.ArgsParser.DYSON_EMAIL;
import static com.artigile.homestats.exporter.ArgsParser.DYSON_PASSROD;

import com.artigile.homestats.DataService;
import com.artigile.homestats.DbDao;
import com.artigile.homestats.sensor.SensorFactory;
import com.artigile.homestats.sensor.SensorMode;
import com.artigile.homestats.sensor.SensorsDataProvider;
import com.artigile.homestats.sensor.dyson.DysonConnect;
import com.google.common.base.Strings;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExporterService {
    static final int PORT = Integer.parseInt(System.getProperty("port", "8086"));
    private static final Logger LOGGER = LoggerFactory.getLogger(ExporterService.class);
    private final static long DB_POLL_INTERVAL = 1000 * 60;

    public static void main(String[] args) throws ParseException {
        new ExporterService().start(args);
    }

    private void start(final String[] args) throws ParseException {
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.isDisplayHelp()) {
            ArgsParser.printHelp();
            return;
        }

        try {
            SensorMode appMode = SensorMode.valueOf(
                argsParser.getString(ArgsParser.APP_MODE_OPTION, "dev").toUpperCase());

            SensorsDataProvider sensorsDataProvider = SensorFactory.buildSensorDataProvider(appMode);
            if (sensorsDataProvider == null) {
                LOGGER.error("No sensor device available, quitting.");
                return;
            }

            final boolean printAndExit = argsParser.argumentPassed(ArgsParser.PRINT_AND_EXIT);
            if (printAndExit) {
                sensorsDataProvider.printAll();
                return;
            }

            final String dbHost = argsParser.getString(DB_HOST_OPTION, "localhost");
            final String user = argsParser.getString(DB_USER_OPTION);
            final String pwd = argsParser.getString(DB_PWD_OPTION);
            final int port = Integer.valueOf(argsParser.getString(APP_PORT_OPTION, PORT + ""));
            final String dbName = argsParser.getString(DB_NAME);

            final String dysonEmail = argsParser.getString(DYSON_EMAIL);
            final String dysonPwd = argsParser.getString(DYSON_PASSROD);
            final DysonConnect dysonConnect;
            if (!Strings.isNullOrEmpty(dysonEmail) && !Strings.isNullOrEmpty(dysonPwd)) {
                dysonConnect = new DysonConnect(dysonEmail, dysonPwd);
            } else {
                dysonConnect = null;
            }

            new DataService(sensorsDataProvider, new DbDao(dbHost, user, pwd, port, dbName), dysonConnect,
                DB_POLL_INTERVAL).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

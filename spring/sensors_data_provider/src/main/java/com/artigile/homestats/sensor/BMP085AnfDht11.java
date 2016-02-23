package com.artigile.homestats.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @author ivanbahdanau
 */
public class BMP085AnfDht11 implements SensorsDataProvider {
    private I2CDevice device;
    private int ac1;
    private int ac2;
    private int ac3;
    private int ac4;
    private int ac5;
    private int ac6;
    private int b1;
    private int b2;
    private long b5;
    @SuppressWarnings("unused")
    private int mb;
    private int mc;
    private int md;
    private int oss;

    private Date lastDhtUpdate;
    private float lastTemp;
    private float lastHum;

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BMP085AnfDht11.class);


    public BMP085AnfDht11() throws IOException {
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = bus.getDevice(0x77);
    }

    public void init() {
        try {
            byte[] eepromData = new byte[22];
            device.read(0xAA, eepromData, 0, 22);
            ac1 = readShort(eepromData, 0);
            ac2 = readShort(eepromData, 2);
            ac3 = readShort(eepromData, 4);
            ac4 = readUShort(eepromData, 6);
            ac5 = readUShort(eepromData, 8);
            ac6 = readUShort(eepromData, 10);
            b1 = readShort(eepromData, 12);
            b2 = readShort(eepromData, 14);
            mb = readShort(eepromData, 16);
            mc = readShort(eepromData, 18);
            md = readShort(eepromData, 20);
        } catch (IOException ignore) {
            ignore.printStackTrace();
        }
    }

    public void startTemperatureRead() throws IOException {
        device.write(0xf4, (byte)0x2e);
    }

    public int readUncompensatedTemperature() throws IOException {
        byte[] t = new byte[2];
        int r = device.read(0xf6, t, 0, 2);
        if (r != 2) {
            throw new IOException("Cannot read temperature; r=" + r);
        }
        int ut = readShort(t, 0);
        return ut;
    }

    public int calculateTemperature(int ut) {
        long x1 = (ut - ac6) * ac5;
        x1 = x1 >>> 15;
        long x2 = (mc << 11) / (x1 + md);
        b5 = x1 + x2;
        int t = (int)((b5 + 8) >>> 4);
        return t;
    }

    public float readTemperature() throws IOException {
        startTemperatureRead();
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignore) { }
        int ut = readUncompensatedTemperature();
        int t = calculateTemperature(ut);
        return (float) (t/10.);
    }

    @Override
    public float readHumidity() throws Exception {
        String cmd = "sudo python /home/pi/DHTReader.py";
        try {
            String ret = "";
            try {
                LOGGER.debug("Starting reading humidity");
                String line;
                Process p = Runtime.getRuntime().exec(cmd.split(" "));
                p.waitFor();
                BufferedReader input = new BufferedReader
                        (new InputStreamReader(p.getInputStream()));

                while ((line = input.readLine()) != null) {
                    ret += (line + '\n');
                }
                input.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ret.trim();
            if (ret.length() == 0) // Library is not present
                throw new RuntimeException("Library not present");
            else {
                LOGGER.debug("Message read from python: " + ret);
                // Error reading the the sensor, maybe is not connected.
                if (ret.contains("   ")) {
                    // Read completed. Parse and update the values
                    String[] vals = ret.split("   ");
                    float t = Float.parseFloat(vals[0].trim());
                    float h = Float.parseFloat(vals[1].trim());
                    if ((t != lastTemp) || (h != lastHum)) {
                        lastDhtUpdate = new Date();
                        lastTemp = t;
                        lastHum = h;
                    }
                } else {
                    String msg = String.format("Error reading message BLABLABLA");
                    throw new Exception(msg);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return lastHum;
    }

    public void startPressureRead() throws IOException {
        device.write(0xf4, (byte)(0x34 + (oss << 6)));
    }

    public int readUncompensatedPressure() throws IOException {
        byte[] p = new byte[3];
        int r = device.read(0xf6, p, 0, 3);
        if (r != 3) {
            throw new IOException("Cannot read pressure; r=" + r);
        }

        int up = ((p[0] & 0xff) << 16) + ((p[1] & 0xff) << 8) +(p[2] & 0xff) >> (8 - oss);
        return up;
    }

    public int calculatePressure(int up) {
        LOGGER.debug("up5=" + up);
        LOGGER.debug("bp5=" + b5);

        long p = 0;
        long b6 = b5 - 4000;
        LOGGER.debug("bp6=" + b6);

        long x1 = (b2 * ((b6 * b6) >> 12)) >> 11;
        LOGGER.debug("x1=" + x1);

        long x2 = (ac2 * b6) >> 11;
        LOGGER.debug("x2=" + x2);

        long x3 = x1 + x2;
        LOGGER.debug("x3=" + x3);

        long b3 = (((ac1 * 4 + x3) << oss) + 2) >> 2;
        LOGGER.debug("b3=" + b3);

        x1 = (ac3 * b6) >> 13;
        LOGGER.debug("x1=" + x1);

        x2 = (b1 * ((b6 * b6) >> 12)) >> 16;
        LOGGER.debug("x3=" + x2);

        x3 = ((x1 + x2) + 2) >> 2;
        LOGGER.debug("x2=" + x3);

        long b4 = (ac4 * (x3 + 32768)) >> 15;
        LOGGER.debug("b4=" + b4);

        long b7 = (up - b3) * (50000 >> oss);
        LOGGER.debug("b7=" + b7);

        if (b7 < 0x80000000) {
            p = (b7 * 2) / b4;
        } else {
            p = (b7 / b4) * 2;
        }
        LOGGER.debug("p=" + p);

        x1 = (p >> 8) * (p >> 8);
        LOGGER.debug("x1=" + x1);

        x1 = (x1 * 3038) >> 16;
        LOGGER.debug("x1=" + x1);

        x2 = (-7357 * p) / 65536;
        LOGGER.debug("x2=" + x2);

        p = p + ((x1 + x2 + 3791) >> 4);
        LOGGER.debug("p=" + p);

        return (int)p;
    }

    public int readPressure() throws IOException {
        startPressureRead();
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignore) { }
        int up = readUncompensatedPressure();
        int p = calculatePressure(up);
        return p;
    }



    public int readShort(byte[] data, int a) {
        int r = (data[a] * 256) + (data[a + 1] & 0xff);
        return r;
    }

    public int readUShort(byte[] data, int a) {
        int r = ((data[a] & 0xff) << 8) + (data[a + 1] & 0xff);
        return r;
    }


    public static void main(String[] args) throws Exception {

        BMP085AnfDht11 bmp085AnfDht11 = new BMP085AnfDht11();

        bmp085AnfDht11.init();
        float t = bmp085AnfDht11.readTemperature();
        System.out.println("Temperature is " + t + " in 0.1C");

        int p = bmp085AnfDht11.readPressure();
        System.out.println("Pressure is    " + p + " in Pascals");
        System.out.println("Pressure is    " + (p / 100d) + " in hPa");

        System.out.println();
        System.out.println();

        double p0 = 1037;
        System.out.println("p0 = " + p0);

        double dp = p / 100d;
        System.out.println("p = " + dp);

        double power = 1d / 5.255d;
        System.out.println("power = " + power);

        double division = dp / p0;
        System.out.println("division = " + division);

        double pw = Math.pow(division, power);
        System.out.println("pw = " + pw);

        double altitude = 44330 * (1 - pw);
        // double p0 = 101325;
        // double altitude = 44330 * (1 - (Math.pow((p / p0), (1 /  5.255))));
        System.out.println();
        System.out.println("Altitude " + altitude + "m");
    }

}

package com.artigile.homestats.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author ivanbahdanau
 */
public class HTU21F implements TempAndHumidity{
    public final static int HTU21DF_ADDRESS = 0x40;
    // HTU21DF Registers
    public final static int HTU21DF_READTEMP = 0xE3;
    public final static int HTU21DF_READHUM  = 0xE5;

    public final static int HTU21DF_READTEMP_NH = 0xF3; // NH = no hold
    public final static int HTU21DF_READHUMI_NH = 0xF5;

    public final static int HTU21DF_WRITEREG = 0xE6;
    public final static int HTU21DF_READREG  = 0xE7;
    public final static int HTU21DF_RESET    = 0xFE;

    private static boolean verbose = "true".equals(System.getProperty("htu21df.verbose", "false"));

    private I2CBus bus;
    private I2CDevice htu21df;

    public HTU21F()
    {
        this(HTU21DF_ADDRESS);
    }

    public HTU21F(int address)
    {
        try
        {
            // Get i2c bus
            bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
            if (verbose)
                System.out.println("Connected to bus. OK.");

            // Get device itself
            htu21df = bus.getDevice(address);
            if (verbose)
                System.out.println("Connected to device. OK.");
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public boolean begin()
            throws Exception
    {
        reset();

        htu21df.write((byte) HTU21DF_READREG);
        int r = htu21df.read();
        if (verbose)
            System.out.println("DBG: Begin: 0x" + lpad(Integer.toHexString(r), "0", 2));

        return (r == 0x02);
    }

    public void reset()
            throws Exception
    {
        //  htu21df.write(HTU21DF_ADDRESS, (byte)HTU21DF_RESET);
        htu21df.write((byte) HTU21DF_RESET);
        if (verbose)
            System.out.println("DBG: Reset OK");
        waitfor(15); // Wait 15ms
    }

    @Override
    public float readTemperature()
            throws Exception
    {
        // Reads the raw temperature from the sensor
        if (verbose)
            System.out.println("Read Temp: Written 0x" + lpad(Integer.toHexString((HTU21DF_READTEMP & 0xff)), "0", 2));
        htu21df.write((byte) (HTU21DF_READTEMP)); //  & 0xff));
        waitfor(50); // Wait 50ms
        byte[] buf = new byte[3];
    /*int rc  = */htu21df.read(buf, 0, 3);
        int msb = buf[0] & 0xFF;
        int lsb = buf[1] & 0xFF;
        int crc = buf[2] & 0xFF;
        int raw = ((msb << 8) + lsb) & 0xFFFC;

        //  while (!Wire.available()) {}

        if (verbose)
        {
            System.out.println("Temp -> 0x" + lpad(Integer.toHexString(msb), "0", 2) + " " + "0x" +
                    lpad(Integer.toHexString(lsb), "0", 2) + " " + "0x" + lpad(Integer.toHexString(crc), "0", 2));
            System.out.println("DBG: Raw Temp: " + (raw & 0xFFFF) + ", " + raw);
        }

        float temp = raw; // t;
        temp *= 175.72;
        temp /= 65536;
        temp -= 46.85;

        if (verbose)
            System.out.println("DBG: Temp: " + temp);
        return temp;
    }

    @Override
    public float readHumidity()
            throws Exception
    {
        // Reads the raw (uncompensated) humidity from the sensor
        htu21df.write((byte) HTU21DF_READHUM);
        waitfor(50); // Wait 50ms
        byte[] buf = new byte[3];
    /* int rc  = */htu21df.read(buf, 0, 3);
        int msb = buf[0] & 0xFF;
        int lsb = buf[1] & 0xFF;
        int crc = buf[2] & 0xFF;
        int raw = ((msb << 8) + lsb) & 0xFFFC;

        //  while (!Wire.available()) {}

        if (verbose)
        {
            System.out.println("Hum -> 0x" + lpad(Integer.toHexString(msb), "0", 2) + " " + "0x" +
                    lpad(Integer.toHexString(lsb), "0", 2) + " " + "0x" + lpad(Integer.toHexString(crc), "0", 2));
            System.out.println("DBG: Raw Humidity: " + (raw & 0xFFFF) + ", " + raw);
        }

        float hum = raw;
        hum *= 125;
        hum /= 65536;
        hum -= 6;

        if (verbose)
            System.out.println("DBG: Humidity: " + hum);
        return hum;
    }

    protected static void waitfor(long howMuch)
    {
        try
        {
            Thread.sleep(howMuch);
        }
        catch (InterruptedException ie)
        {
            ie.printStackTrace();
        }
    }

    private static String lpad(String s, String with, int len)
    {
        String str = s;
        while (str.length() < len)
            str = with + str;
        return str;
    }

    public static void main(String[] args)
    {
        final NumberFormat NF = new DecimalFormat("##00.00");
        HTU21F sensor = new HTU21F();
        float hum = 0;
        float temp = 0;

        try
        {
            if (!sensor.begin())
            {
                System.out.println("Sensor not found!");
                System.exit(1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }

        try
        {
            hum = sensor.readHumidity();
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }

        try
        {
            temp = sensor.readTemperature();
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Temperature: " + NF.format(temp) + " C");
        System.out.println("Humidity   : " + NF.format(hum) + " %");
    }
}

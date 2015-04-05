import time
import sys

import MySQLdb

# import BMP085 as BMP085



# How long to wait (in seconds) between measurements.
FREQUENCY_SECONDS = 300



is_real_data = len(sys.argv) > 1 and str(sys.argv[1]) == 'prod'
if is_real_data:
    print 'Starting the app in production mode'
    import BMP085 as BMP085
    import Adafruit_DHT
    # Create sensor instance with default I2C bus (On Raspberry Pi either 0 or
    # 1 based on the revision, on Beaglebone Black default to 1).
    bmp = BMP085.BMP085()

print 'Logging sensor measurements to MySql every {0} seconds.'.format(FREQUENCY_SECONDS)
print 'Press Ctrl-C to quit.'


def save_data_to_mysql(temp, pressure, altitude, humidity):
    try:
        db = MySQLdb.connect(passwd='root', db='ivannaroom', user='root')
        cursor = db.cursor()
        insert_room_data = """insert into sensor_stats (id,temperature,pressure,altitude,humidity) values(now(),%s,%s,%s,%s);"""
        room_data = (temp, pressure, altitude, humidity)
        cursor.execute(insert_room_data, room_data)
        db.commit()
        cursor.close()
        db.close()
    except MySQLdb.Error, e:
        print "Error %d: %s" % (e.args[0], e.args[1])

while True:
    # Attempt to get sensor readings.
    if is_real_data:
        temp = bmp.read_temperature()
        pressure = bmp.read_pressure()
        altitude = bmp.read_altitude()
        humidity, temperature = Adafruit_DHT.read_retry(Adafruit_DHT.DHT11, 4)
    else:
        temp = 24.6
        pressure = 234.3
        altitude = 443.6
        humidity = 40

    print 'Temperature: {0:0.1f} C'.format(temp)
    print 'Pressure:    {0:0.1f} Pa'.format(pressure)
    print 'Altitude:    {0:0.1f} m'.format(altitude)
    print 'Humidity:    {0:0.1f} m'.format(humidity)

    # Append the data in the spreadsheet, including a timestamp
    try:
        save_data_to_mysql(temp, pressure, altitude, humidity)
    except:
        # Error appending data, most likely because credentials are stale.
        print 'Append error, logging in again'
        time.sleep(FREQUENCY_SECONDS)
        continue

    # Wait 30 seconds before continuing
    print "Wrote a row time: %s, temp: %s, pressure: %s, humidity: %s" % (temp, pressure, altitude, humidity)
    time.sleep(FREQUENCY_SECONDS)






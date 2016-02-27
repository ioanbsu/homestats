# Spring implementation of home stats service.
 
 It uses spring boot and spring services. 
 Modules used in project:
 
 * config_server the config server module that provides information about configuration to all other modules.
 The config itself reading all the configurations from github repository https://github.com/ioanbsu/homestats-config . 
 All configurations are dynamically reloadable so to change the configuration you only need to push the updated files 
 to this repository and the config_server will pick it up right away. The other modules that are using config_server won't 
 pick up updated configurations right away on a fly since they cache configuration, you need either to restart the application
 or cal specific endpoint(google what endpoint to call) to tell the app to refresh the configuration. To view configuration open 
 in browser http://localhost:8888/{configuration}/master for example http://localhost:8888/roomsensors/master. By default config-service
 is runing on port 8888, if changed all modules that using config-service needed to be updated appropriately.
  
 * eurika-service the netflix's service discovery module. Basically it is used to provide information on what services are 
 available. There are currently two services that are using discovery service: room_sensors and web - they both can be found at the url
 http://localhost:8761/ when successfully started.
 
 * hystrix-dashabord the simple dashboard to display services stats. To view the stats open http://localhost:7000/hystrix.html
 
 * model - simple the shared POJO model that is used by various other modules to transit data between each other.
 
 * room_sensors the service that exposes information from the sensors. This service requires to be ran at the device that has sensors.
 If it ran on module where there are no sensors the "default" spring profile needs to be used so this service will provide fake data.
 This module requires connection to mysql. Please configure appropriately either by updating application.properties file or appropriate roomsensors*.properties file.
 The database with following scheme is required so this module can get started: 
 `CREATE TABLE sensor_stats
  (
      id DATETIME PRIMARY KEY NOT NULL,
      temperature FLOAT(7,4) NOT NULL,
      humidity FLOAT(7,4) NOT NULL,
      pressure BIGINT(20) DEFAULT '0' NOT NULL
  );
  CREATE INDEX humidity ON sensor_stats (humidity);
  CREATE INDEX pressure ON sensor_stats (pressure);
  CREATE INDEX temperature ON sensor_stats (temperature);
  `
 
 
 * sensors_data_provider the module that provides interface for talking to the devices and returns the data from them. Currently two 
 devices sets are available: BMP085+DHT11 and HTU21F. 
 
 * web - the web interface to expose the historical data and current sensors readings.
 
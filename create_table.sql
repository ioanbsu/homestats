create database ivannaroom default charset=utf8;
use ivannarom;
CREATE TABLE sensor_stats
(
  id DATETIME PRIMARY KEY NOT NULL,
  temperature float(7,4) NOT NULL,
  humidity float(7,4) NOT NULL
);
CREATE INDEX temperature on sensor_stats(temperature);
CREATE INDEX humidity on sensor_stats(humidity);

ALTER TABLE sensor_stats add pressure BIGINT NOT NULL DEFAULT 0;
CREATE INDEX pressure on sensor_stats(pressure);



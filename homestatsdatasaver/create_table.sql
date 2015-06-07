CREATE TABLE sensor_stats
(
  id DATETIME PRIMARY KEY NOT NULL,
  temperature float(7,4) NOT NULL,
  humidity float(7,4) NOT NULL
);
CREATE INDEX temperature on sensor_stats(temperature);
CREATE INDEX pressure on sensor_stats(pressure);
CREATE INDEX humidity on sensor_stats(pressure);



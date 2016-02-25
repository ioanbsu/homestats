package com.artigile.homestats.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "sensor_stats")
public class SensorData {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mmZ";
    @Id
    private Date id;

    private Float temperature;

    private Float humidity;

    private Integer pressure;

    public Date getId() {
        return id;
    }

    public void setId(Date id) {
        this.id = id;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                '}';
    }

    public Float[] toDataArray() {
        return new Float[]{Float.valueOf(getId().getTime()/1000),
                getTemperature() ,
                getHumidity() ,
                Float.valueOf(getPressure())
        };
    }
}

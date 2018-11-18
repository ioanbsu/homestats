package com.artigile.homestats.sensor.dyson.model;

import java.time.Instant;

public class SensorData {
    public static final String KEY = "ENVIRONMENTAL-CURRENT-SENSOR-DATA";

    /**
     *
     */
    public final Instant instant;
    /**
     * Temp in inCelcius
     */
    public final double tempCelsius;
    /**
     * Humidity %
     */
    public final int hact;

    /**
     * pm25
     */
    public final int pm25;
    /**
     * pm10
     */
    public final int pm10;
    /**
     *
     */
    public final int va10;
    /**
     *
     */
    public final int noxl;
    /**
     *
     */
    public final int p25r;
    /**
     *
     */
    public final int p10r;
    /**
     * Sleep timer.
     */
    public final boolean sltm;

    public Instant builtAt;

    private SensorData(final Builder builder) {
        this.instant = builder.instant;
        this.tempCelsius = builder.tempCelsius;
        this.hact = builder.hact;
        this.pm25 = builder.pm25;
        this.pm10 = builder.pm10;
        this.va10 = builder.va10;
        this.noxl = builder.noxl;
        this.p25r = builder.p25r;
        this.p10r = builder.p10r;
        this.sltm = builder.sltm;
        this.builtAt = Instant.now();
    }

    @Override
    public String toString() {
        return "SensorData{" +
            "instant=" + instant +
            ", tempCelsius=" + tempCelsius +
            ", hact=" + hact +
            ", pm25=" + pm25 +
            ", pm10=" + pm10 +
            ", va10=" + va10 +
            ", noxl=" + noxl +
            ", p25r=" + p25r +
            ", p10r=" + p10r +
            ", sltm=" + sltm +
            '}';
    }

    public static class Builder {

        private Instant instant;
        private double tempCelsius;
        private int hact;
        private int pm25;
        private int pm10;
        private int va10;
        private int noxl;
        private int p25r;
        private int p10r;
        private boolean sltm;

        public Builder withDate(final Instant instant) {
            this.instant = instant;
            return this;
        }

        public Builder withTempCelsius(final double tempCelsius) {
            this.tempCelsius = tempCelsius;
            return this;
        }

        public Builder withHact(final int hact) {
            this.hact = hact;
            return this;
        }

        public Builder withPm25(final int pm25) {
            this.pm25 = pm25;
            return this;
        }

        public Builder withPm10(final int pm10) {
            this.pm10 = pm10;
            return this;
        }

        public Builder withVa10(final int va10) {
            this.va10 = va10;
            return this;
        }

        public Builder withNoxl(final int noxl) {
            this.noxl = noxl;
            return this;
        }

        public Builder withP25r(final int p25r) {
            this.p25r = p25r;
            return this;
        }

        public Builder withP10r(final int p10r) {
            this.p10r = p10r;
            return this;
        }

        public Builder withSltm(final boolean sltm) {
            this.sltm = sltm;
            return this;
        }

        public SensorData build() {
            return new SensorData(this);
        }
    }
}

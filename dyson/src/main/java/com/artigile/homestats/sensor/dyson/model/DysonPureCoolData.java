package com.artigile.homestats.sensor.dyson.model;

import java.time.Instant;

/**
 * Dyson pure cool data.
 */
public class DysonPureCoolData {

    public final SensorData currentSensorData;
    public final State currentState;
    public final Instant lastRefreshed;

    public DysonPureCoolData(final Builder builder) {
        this.currentSensorData = builder.currentSensorData;
        this.currentState = builder.currentState;
        this.lastRefreshed = builder.lastRefreshed;
    }

    public static class Builder {
        private SensorData currentSensorData;
        private State currentState;
        private Instant lastRefreshed;

        public Builder withCurrentSensorData(final SensorData currentSensorData) {
            this.currentSensorData = currentSensorData;
            return this;
        }

        public Builder withCurrentState(final State currentState) {
            this.currentState = currentState;
            return this;
        }

        public DysonPureCoolData build() {
            this.lastRefreshed = Instant.now();
            return new DysonPureCoolData(this);
        }
    }

}

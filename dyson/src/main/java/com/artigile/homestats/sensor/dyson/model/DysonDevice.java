package com.artigile.homestats.sensor.dyson.model;

public class DysonDevice {

    public final DysonPureCoolData dysonPureCoolData;
    public final DeviceDescription deviceDescription;

    public DysonDevice(final Builder builder) {
        this.dysonPureCoolData = builder.dysonPureCoolData;
        this.deviceDescription = builder.deviceDescription;
    }

    public final static class Builder {

        private DysonPureCoolData dysonPureCoolData;
        private DeviceDescription deviceDescription;

        public Builder withDysonPureCoolData(final DysonPureCoolData dysonPureCoolData) {
            this.dysonPureCoolData = dysonPureCoolData;
            return this;
        }

        public Builder withDeviceDescription(final DeviceDescription deviceDescription) {
            this.deviceDescription = deviceDescription;
            return this;
        }

        public DysonDevice build() {
            return new DysonDevice(this);
        }
    }
}

package com.artigile.homestats.sensor.dyson.model;

public class DeviceDescription {
    public final String serial;
    public final String connectionType;
    public final String version;
    public final boolean autoUpdate;
    public final boolean newVersionAvailable;
    public final String productType;
    public final LocalCredentials localCredentials;
    public final String name;

    public DeviceDescription(final Builder builder) {
        this.serial = builder.serial;
        this.connectionType = builder.connectionType;
        this.version = builder.version;
        this.autoUpdate = builder.autoUpdate;
        this.newVersionAvailable = builder.newVersionAvailable;
        this.productType = builder.productType;
        this.localCredentials = builder.localCredentials;
        this.name = builder.name;
    }

    @Override
    public String toString() {
        return "DeviceDescription{" +
            "serial='" + serial + '\'' +
            ", connectionType='" + connectionType + '\'' +
            ", version='" + version + '\'' +
            ", autoUpdate=" + autoUpdate +
            ", newVersionAvailable=" + newVersionAvailable +
            ", productType='" + productType + '\'' +
            ", name='" + name + '\'' +
            '}';
    }

    public final static class Builder {

        private String serial;
        private String connectionType;
        private String version;
        private boolean autoUpdate;
        private boolean newVersionAvailable;
        private String productType;
        private LocalCredentials localCredentials;
        private String name;

        public Builder withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        public Builder withConnectionType(final String connectionType) {
            this.connectionType = connectionType;
            return this;
        }

        public Builder withVersion(final String version) {
            this.version = version;
            return this;
        }

        public Builder withAutoUpdate(final boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            return this;
        }

        public Builder withNewVersionAvailable(final boolean newVersionAvailable) {
            this.newVersionAvailable = newVersionAvailable;
            return this;
        }

        public Builder withProductType(final String productType) {
            this.productType = productType;
            return this;
        }

        public Builder withLocalCredentials(final LocalCredentials localCredentials) {
            this.localCredentials = localCredentials;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public DeviceDescription build() {
            return new DeviceDescription(this);
        }

    }

    public static final class LocalCredentials {

        public final String passwordHash;
        public final String serial;

        public LocalCredentials(final Builder builder) {
            this.passwordHash = builder.passwordHash;
            this.serial = builder.serial;
        }

        public static class Builder {

            private String passwordHash;
            private String serial;

            public Builder withPasswordHash(final String passwordHash) {
                this.passwordHash = passwordHash;
                return this;
            }

            public Builder withSerial(final String serial) {
                this.serial = serial;
                return this;
            }

            public LocalCredentials build() {
                return new LocalCredentials(this);
            }

        }

    }

}

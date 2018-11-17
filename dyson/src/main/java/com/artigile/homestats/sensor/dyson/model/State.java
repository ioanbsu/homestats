package com.artigile.homestats.sensor.dyson.model;

import java.time.Instant;

public class State {
    public static final String KEY = "CURRENT-STATE";
    public final Instant instant;
    public final String modeReason;
    public final boolean dial;
    public final int rssi;
    public final int channel;
    public final ProductState productState;

    public State(final Builder builder) {
        this.instant
            = builder.instant
        ;
        this.modeReason = builder.modeReason;
        this.dial = builder.dial;
        this.rssi = builder.rssi;
        this.channel = builder.channel;
        this.productState = builder.productState;
    }

    public static class Builder {
        private Instant instant;
        private String modeReason;
        private boolean dial;
        private int rssi;
        private int channel;
        private ProductState productState;

        public Builder withDate(final Instant instant
        ) {
            this.instant
                = instant
            ;
            return this;
        }

        public Builder withModeReason(final String modereason) {
            this.modeReason = modereason;
            return this;
        }

        public Builder withDial(final boolean dial) {
            this.dial = dial;
            return this;
        }

        public Builder withRssi(final int rssi) {
            this.rssi = rssi;
            return this;
        }

        public Builder withChannel(final int channel) {
            this.channel = channel;
            return this;
        }

        public Builder withProductState(final ProductState productstate) {
            this.productState = productstate;
            return this;
        }

        public State build() {
            return new State(this);
        }
    }

    public static class ProductState {
        private final boolean fpwr;
        private final boolean fdir;
        private final boolean auto;
        private final boolean oscs;
        private final boolean oson;
        private final boolean nmod;
        private final boolean rhtm;
        private final String fnst;
        private final String ercd;
        private final String wacd;
        private final int nmdv;
        /**
         * Fan Speed. Can be 0-10 or "AUTO"
         */
        private final String fnsp;
        private final int bril;
        private final boolean corf;
        private final int cflr;
        private final int hflr;
        private final boolean sltm;
        private final int osal;
        private final int osau;
        private final int ancp;

        public ProductState(final ProductState.Builder builder) {
            this.fpwr = builder.fpwr;
            this.fdir = builder.fdir;
            this.auto = builder.auto;
            this.oscs = builder.oscs;
            this.oson = builder.oson;
            this.nmod = builder.nmod;
            this.rhtm = builder.rhtm;
            this.fnst = builder.fnst;
            this.ercd = builder.ercd;
            this.wacd = builder.wacd;
            this.nmdv = builder.nmdv;
            this.fnsp = builder.fnsp;
            this.bril = builder.bril;
            this.corf = builder.corf;
            this.cflr = builder.cflr;
            this.hflr = builder.hflr;
            this.sltm = builder.sltm;
            this.osal = builder.osal;
            this.osau = builder.osau;
            this.ancp = builder.ancp;
        }

        public boolean isFpwr() {
            return fpwr;
        }

        public boolean isFdir() {
            return fdir;
        }

        public boolean isAuto() {
            return auto;
        }

        public boolean isOscs() {
            return oscs;
        }

        public boolean isOson() {
            return oson;
        }

        public boolean isNmod() {
            return nmod;
        }

        public boolean isRhtm() {
            return rhtm;
        }

        public String fnst() {
            return fnst;
        }

        public String ercd() {
            return ercd;
        }

        public String wacd() {
            return wacd;
        }

        public int nmdv() {
            return nmdv;
        }

        public String fnsp() {
            return fnsp;
        }

        public int bril() {
            return bril;
        }

        public boolean isCorf() {
            return corf;
        }

        public int cflr() {
            return cflr;
        }

        public int hflr() {
            return hflr;
        }

        public boolean isSltm() {
            return sltm;
        }

        public int osal() {
            return osal;
        }

        public int osau() {
            return osau;
        }

        public int ancp() {
            return ancp;
        }

        public static class Builder {
            private boolean fpwr;
            private boolean fdir;
            private boolean auto;
            private boolean oscs;
            private boolean oson;
            private boolean nmod;
            private boolean rhtm;
            private String fnst;
            private String ercd;
            private String wacd;
            private int nmdv;
            private String fnsp;
            private int bril;
            private boolean corf;
            private int cflr;
            private int hflr;
            private boolean sltm;
            private int osal;
            private int osau;
            private int ancp;

            public ProductState.Builder withFpwr(final boolean fpwr) {
                this.fpwr = fpwr;
                return this;
            }

            public ProductState.Builder withFdir(final boolean fdir) {
                this.fdir = fdir;
                return this;
            }

            public ProductState.Builder withAuto(final boolean auto) {
                this.auto = auto;
                return this;
            }

            public ProductState.Builder withOscs(final boolean oscs) {
                this.oscs = oscs;
                return this;
            }

            public ProductState.Builder withOson(final boolean oson) {
                this.oson = oson;
                return this;
            }

            public ProductState.Builder withNmod(final boolean nmod) {
                this.nmod = nmod;
                return this;
            }

            public ProductState.Builder withRhtm(final boolean rhtm) {
                this.rhtm = rhtm;
                return this;
            }

            public ProductState.Builder withFnst(final String fnst) {
                this.fnst = fnst;
                return this;
            }

            public ProductState.Builder withErcd(final String ercd) {
                this.ercd = ercd;
                return this;
            }

            public ProductState.Builder withWacd(final String wacd) {
                this.wacd = wacd;
                return this;
            }

            public ProductState.Builder withNmdv(final int nmdv) {
                this.nmdv = nmdv;
                return this;
            }

            public ProductState.Builder withFnsp(final String fnsp) {
                this.fnsp = fnsp;
                return this;
            }

            public ProductState.Builder withBril(final int bril) {
                this.bril = bril;
                return this;
            }

            public ProductState.Builder withCorf(final boolean corf) {
                this.corf = corf;
                return this;
            }

            public ProductState.Builder withCflr(final int cflr) {
                this.cflr = cflr;
                return this;
            }

            public ProductState.Builder withHflr(final int hflr) {
                this.hflr = hflr;
                return this;
            }

            public ProductState.Builder withSltm(final boolean sltm) {
                this.sltm = sltm;
                return this;
            }

            public ProductState.Builder withOsal(final int osal) {
                this.osal = osal;
                return this;
            }

            public ProductState.Builder withOsau(final int osau) {
                this.osau = osau;
                return this;
            }

            public ProductState.Builder withAncp(final int ancp) {
                this.ancp = ancp;
                return this;
            }

            public ProductState build() {
                return new ProductState(this);
            }

        }

    }
}

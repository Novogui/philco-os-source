package com.philco.ultra777.home;

/** Central RSSI classifier for the Home and idle screensaver indicators. */
final class WifiSignal {
    static final int RSSI_INVALID = -127;

    final boolean connected;
    final boolean rssiAvailable;
    final int rssiDbm;
    final int bars;
    final String label;

    private WifiSignal(boolean connected, boolean rssiAvailable, int rssiDbm,
                       int bars, String label) {
        this.connected = connected;
        this.rssiAvailable = rssiAvailable;
        this.rssiDbm = rssiDbm;
        this.bars = bars;
        this.label = label;
    }

    static WifiSignal disconnected() {
        return new WifiSignal(false, false, RSSI_INVALID, -1, "Sem Wi-Fi");
    }

    static WifiSignal unavailable() {
        return new WifiSignal(true, false, RSSI_INVALID, 0, "Wi-Fi indisponível");
    }

    static WifiSignal fromRssi(int rssiDbm) {
        if (rssiDbm == RSSI_INVALID || rssiDbm < -127 || rssiDbm > 0) {
            return unavailable();
        }
        if (rssiDbm >= -60) return new WifiSignal(true, true, rssiDbm, 4, "Wi-Fi Forte");
        if (rssiDbm >= -67) return new WifiSignal(true, true, rssiDbm, 3, "Wi-Fi Bom");
        if (rssiDbm >= -75) return new WifiSignal(true, true, rssiDbm, 2, "Wi-Fi Regular");
        return new WifiSignal(true, true, rssiDbm, 1, "Wi-Fi Fraco");
    }
}

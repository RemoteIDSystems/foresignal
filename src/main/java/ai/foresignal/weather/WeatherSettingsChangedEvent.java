package ai.foresignal.weather;

/**
 * Published after {@code PUT /v1/settings/weather} stored new settings (prompt 056). The places AOI recomputes on
 * it when {@code defaultAoi} or {@code aoiBufferKm} changed.
 *
 * @param previous the settings before
 * @param current the settings now
 */
public record WeatherSettingsChangedEvent(WeatherSettings previous, WeatherSettings current) {

    /** True when the AOI inputs changed. */
    public boolean aoiChanged() {
        return previous == null || !previous.defaultAoi().equals(current.defaultAoi())
                || Double.compare(previous.aoiBufferKm(), current.aoiBufferKm()) != 0;
    }
}

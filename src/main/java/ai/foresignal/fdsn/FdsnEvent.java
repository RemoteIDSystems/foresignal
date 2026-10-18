package ai.foresignal.fdsn;

import java.time.Instant;
import java.util.Objects;

/**
 * One earthquake as the FDSN writers see it (prompt 141), read from a current {@code earthquake} record.
 *
 * <p>The payload keys follow the shared earthquake shape of prompts 011 and 089: {@code mag}, {@code magType},
 * {@code place}, depth in km ({@code depth} from USGS, {@code depthKm} from EMSC), {@code auth} or {@code net} as the
 * author, {@code sourceCatalog} as the catalog, and USGS's {@code type} as the event type.
 *
 * @param sourceId the Foresignal source
 * @param externalId the event id within the source
 * @param time the origin time
 * @param latitude the epicenter latitude
 * @param longitude the epicenter longitude
 * @param depthKm the depth in km, or null
 * @param magnitude the magnitude, or null
 * @param magnitudeType the magnitude type, or null
 * @param place the region or location name, or null
 * @param author the reporting agency, or null
 * @param catalog the catalog, or null
 * @param eventType the QuakeML event type (always one of the BED enumeration)
 * @param updated the record's observed time (last update), or null
 */
public record FdsnEvent(
        String sourceId,
        String externalId,
        Instant time,
        double latitude,
        double longitude,
        Double depthKm,
        Double magnitude,
        String magnitudeType,
        String place,
        String author,
        String catalog,
        String eventType,
        Instant updated) {

    /** Requires the identity, the origin time and an event type. */
    public FdsnEvent {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(time, "time");
        eventType = eventType == null ? "earthquake" : eventType;
    }
}

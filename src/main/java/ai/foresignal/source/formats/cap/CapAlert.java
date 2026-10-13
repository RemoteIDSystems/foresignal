package ai.foresignal.source.formats.cap;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * One parsed OASIS CAP {@code alert} (CAP 1.2, or 1.1 with {@link #capVersion()} recorded), as immutable
 * records (prompt 067).
 *
 * <p>Element names follow the CAP 1.2 specification; a repeatable element becomes a list, in document
 * order. Times keep the offset the sender wrote. Coordinates stay as the text CAP carries
 * ({@code "lat,lon"} pairs); {@link CapGeometry} turns them into JTS geometry.
 *
 * @param capVersion {@code "1.2"} or {@code "1.1"}, from the namespace
 * @param identifier the message identifier (required)
 * @param sender the sender (required)
 * @param sent when the message was sent (required)
 * @param status {@code Actual}, {@code Exercise}, {@code System}, {@code Test} or {@code Draft} (required)
 * @param msgType {@code Alert}, {@code Update}, {@code Cancel}, {@code Ack} or {@code Error} (required)
 * @param source the originating source, or null
 * @param scope {@code Public}, {@code Restricted} or {@code Private} (required)
 * @param restriction the restriction text, or null
 * @param addresses the addresses text, or null
 * @param codes the {@code code} elements
 * @param note the note, or null
 * @param references the earlier messages this one refers to, parsed from {@code sender,identifier,sent}
 *     triples
 * @param incidents the {@code incidents} text, or null
 * @param infos the {@code info} blocks
 */
public record CapAlert(
        String capVersion,
        String identifier,
        String sender,
        OffsetDateTime sent,
        String status,
        String msgType,
        String source,
        String scope,
        String restriction,
        String addresses,
        List<String> codes,
        String note,
        List<Reference> references,
        String incidents,
        List<Info> infos) {

    /** Copies the lists. */
    public CapAlert {
        codes = codes == null ? List.of() : List.copyOf(codes);
        references = references == null ? List.of() : List.copyOf(references);
        infos = infos == null ? List.of() : List.copyOf(infos);
    }

    /** The first info block, or null when there is none (a {@code Cancel} may carry none). */
    public Info firstInfo() {
        return infos.isEmpty() ? null : infos.get(0);
    }

    /** {@code {sender}:{identifier}}, the message's own thread key. */
    public String ownKey() {
        return sender + ":" + identifier;
    }

    /**
     * One {@code references} entry.
     *
     * @param sender the referenced sender
     * @param identifier the referenced identifier
     * @param sent the referenced sent time, or null when it is missing or unreadable
     */
    public record Reference(String sender, String identifier, OffsetDateTime sent) {

        /** {@code {sender}:{identifier}}. */
        public String key() {
            return sender + ":" + identifier;
        }
    }

    /**
     * A {@code valueName}/{@code value} pair: {@code eventCode}, {@code parameter} and {@code geocode}.
     *
     * @param valueName the name
     * @param value the value
     */
    public record NamedValue(String valueName, String value) {}

    /**
     * One {@code info} block.
     *
     * @param language the language; {@code en-US} when the element is absent (CAP 1.2 §3.2.2)
     * @param categories the {@code category} values
     * @param event the event text
     * @param responseTypes the {@code responseType} values
     * @param urgency the urgency
     * @param severity the severity
     * @param certainty the certainty
     * @param audience the audience, or null
     * @param eventCodes the event codes
     * @param effective when the information becomes effective, or null
     * @param onset the expected onset of the hazard, or null
     * @param expires when the information expires, or null
     * @param senderName the human-readable sender name, or null
     * @param headline the headline, or null
     * @param description the description, or null
     * @param instruction the instruction, or null
     * @param web the web link, or null
     * @param contact the contact, or null
     * @param parameters the parameters
     * @param resources the resources
     * @param areas the areas
     */
    public record Info(
            String language,
            List<String> categories,
            String event,
            List<String> responseTypes,
            String urgency,
            String severity,
            String certainty,
            String audience,
            List<NamedValue> eventCodes,
            OffsetDateTime effective,
            OffsetDateTime onset,
            OffsetDateTime expires,
            String senderName,
            String headline,
            String description,
            String instruction,
            String web,
            String contact,
            List<NamedValue> parameters,
            List<Resource> resources,
            List<Area> areas) {

        /** Copies the lists. */
        public Info {
            categories = categories == null ? List.of() : List.copyOf(categories);
            responseTypes = responseTypes == null ? List.of() : List.copyOf(responseTypes);
            eventCodes = eventCodes == null ? List.of() : List.copyOf(eventCodes);
            parameters = parameters == null ? List.of() : List.copyOf(parameters);
            resources = resources == null ? List.of() : List.copyOf(resources);
            areas = areas == null ? List.of() : List.copyOf(areas);
        }

        /** Every value of the parameter {@code name}, in document order. */
        public List<String> parameter(String name) {
            return parameters.stream().filter(p -> name.equals(p.valueName())).map(NamedValue::value).toList();
        }
    }

    /**
     * One {@code resource}.
     *
     * @param resourceDesc the description
     * @param mimeType the media type
     * @param size the size in bytes, or null
     * @param uri the URI, or null
     * @param digest the SHA-1 digest, or null
     */
    public record Resource(String resourceDesc, String mimeType, Long size, String uri, String digest) {}

    /**
     * One {@code area}.
     *
     * @param areaDesc the description
     * @param polygons the {@code polygon} texts: space-separated {@code lat,lon} pairs
     * @param circles the {@code circle} texts: {@code lat,lon radiusKm}
     * @param geocodes the geocodes
     * @param altitude the altitude in feet, or null
     * @param ceiling the ceiling in feet, or null
     */
    public record Area(
            String areaDesc,
            List<String> polygons,
            List<String> circles,
            List<NamedValue> geocodes,
            Double altitude,
            Double ceiling) {

        /** Copies the lists. */
        public Area {
            polygons = polygons == null ? List.of() : List.copyOf(polygons);
            circles = circles == null ? List.of() : List.copyOf(circles);
            geocodes = geocodes == null ? List.of() : List.copyOf(geocodes);
        }
    }
}

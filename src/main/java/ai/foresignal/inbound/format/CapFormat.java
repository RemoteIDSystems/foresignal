package ai.foresignal.inbound.format;

import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * The {@code cap} ingest format (prompt 024): one OASIS CAP 1.2 alert per request.
 *
 * <p>{@code Alert} creates the record {@code identifier}; {@code Update} creates it and tombstones
 * every referenced message; {@code Cancel} only tombstones the referenced messages. A status other
 * than {@code Actual} is rejected unless the option {@code acceptNonActual} is true. Every record of
 * the request has index 0.
 */
@Component
public class CapFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "cap";

    private final CapParser parser = new CapParser();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.parseMediaType("application/cap+xml"), MediaType.APPLICATION_XML);
    }

    @Override
    public String description() {
        return "OASIS CAP 1.2 alert (XML; DTDs and external entities rejected)";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(new FormatOptionSpec("acceptNonActual", "boolean", false, false,
                "Accept alerts whose status is not Actual (Exercise, System, Test, Draft)"));
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        CapAlert alert = parser.parse(request.body());
        ParsedIngest.Builder out = ParsedIngest.builder().items(1);
        String identifier = alert.identifier();
        if (identifier == null || identifier.isBlank()) {
            return out.reject(0, null, "EXTERNAL_ID_INVALID", "the alert has no identifier").build();
        }
        if (!"Actual".equals(alert.status()) && !options.bool("acceptNonActual", false)) {
            return out.reject(0, identifier, "NON_ACTUAL_STATUS", "status is " + alert.status()).build();
        }
        String kind = GeoJsonIngestMapper.resolveKind(null, descriptor);
        if (kind == null) {
            return out.reject(0, identifier, "UNKNOWN_KIND", "the source does not have a single kind").build();
        }
        String msgType = alert.msgType() == null ? "Alert" : alert.msgType();
        if ("Update".equals(msgType) || "Cancel".equals(msgType)) {
            for (CapAlert.Reference reference : alert.references()) {
                if (!reference.identifier().equals(identifier)) {
                    out.add(0, NormalizedRecord.tombstone(reference.identifier(), kind, alert.sent()));
                }
            }
        }
        if (!"Cancel".equals(msgType)) {
            out.add(0, record(alert, identifier, kind));
        }
        return out.build();
    }

    private NormalizedRecord record(CapAlert alert, String identifier, String kind) {
        CapAlert.Info info = alert.firstInfo();
        Instant eventTime = info == null ? null : (info.onset() != null ? info.onset() : info.effective());
        NormalizedRecord.Builder record = NormalizedRecord.builder(identifier, kind)
                .observedAt(alert.sent())
                .eventTime(eventTime)
                .validUntil(info == null ? null : info.expires())
                .geometry(alert.geometry(parser.factory()))
                .put("sender", alert.sender())
                .put("status", alert.status())
                .put("msgType", alert.msgType());
        if (info != null) {
            record.put("event", info.event())
                    .put("headline", info.headline())
                    .put("description", info.description())
                    .put("instruction", info.instruction())
                    .put("severity", info.severity())
                    .put("urgency", info.urgency())
                    .put("certainty", info.certainty())
                    .put("language", info.language());
            Set<String> areaDesc = new LinkedHashSet<>();
            List<Map<String, Object>> geocodes = new ArrayList<>();
            for (CapAlert.Area area : info.areas()) {
                if (area.areaDesc() != null && !area.areaDesc().isBlank()) {
                    areaDesc.add(area.areaDesc());
                }
                for (CapAlert.Geocode geocode : area.geocodes()) {
                    Map<String, Object> code = new LinkedHashMap<>();
                    code.put("valueName", geocode.valueName());
                    code.put("value", geocode.value());
                    geocodes.add(code);
                }
            }
            if (!areaDesc.isEmpty()) {
                record.put("areaDesc", String.join("; ", areaDesc));
            }
            if (!geocodes.isEmpty()) {
                record.put("geocodes", geocodes);
            }
        }
        return record.build();
    }
}

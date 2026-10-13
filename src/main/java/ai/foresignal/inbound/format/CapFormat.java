package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapMappingProfile;
import ai.foresignal.source.formats.cap.CapParseOptions;
import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.formats.cap.CapRecordMapper;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
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
 *
 * <p>Since prompt 067 the document is read by the shared {@link CapParser} (hardened StAX, CAP 1.2 and
 * 1.1 namespaces only, required elements enforced, 2 MB cap) and the record is built by the shared
 * {@link CapRecordMapper}, so the time mapping ({@code validFrom = onset ?? effective},
 * {@code validUntil = expires}) and the payload match every built-in CAP source. A parse failure is a
 * 400 ({@link InvalidInputException}). The push format keeps 024's identity rules: the record id is the
 * message {@code identifier}, not a thread key, because a push source has no adapter state to resolve
 * threads with.
 */
@Component
public class CapFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "cap";

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
        CapAlert alert;
        try {
            alert = CapParser.parse(new ByteArrayInputStream(request.body() == null ? new byte[0] : request.body()),
                    CapParseOptions.DEFAULTS);
        } catch (UpstreamFormatException e) {
            throw new InvalidInputException("The CAP document could not be parsed: " + e.getMessage(), e);
        }
        ParsedIngest.Builder out = ParsedIngest.builder().items(1);
        String identifier = alert.identifier();
        if (!"Actual".equals(alert.status()) && !options.bool("acceptNonActual", false)) {
            return out.reject(0, identifier, "NON_ACTUAL_STATUS", "status is " + alert.status()).build();
        }
        String kind = GeoJsonIngestMapper.resolveKind(null, descriptor);
        if (kind == null) {
            return out.reject(0, identifier, "UNKNOWN_KIND", "the source does not have a single kind").build();
        }
        Instant sent = alert.sent().toInstant();
        String msgType = alert.msgType();
        if ("Update".equals(msgType) || "Cancel".equals(msgType)) {
            for (CapAlert.Reference reference : alert.references()) {
                if (!reference.identifier().equals(identifier)) {
                    out.add(0, NormalizedRecord.tombstone(reference.identifier(), kind, sent));
                }
            }
        }
        if (!"Cancel".equals(msgType)) {
            out.add(0, CapRecordMapper.toRecord(alert, identifier, CapMappingProfile.of(kind)));
        }
        return out.build();
    }
}

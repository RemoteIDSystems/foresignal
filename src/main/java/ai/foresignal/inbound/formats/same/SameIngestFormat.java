package ai.foresignal.inbound.formats.same;

import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * The {@code same} ingest format (prompt 086): NOAA Weather Radio SAME headers as {@value #KIND} records, at
 * {@code POST /v1/ingest/{sourceId}/same} with a {@code text/plain} body.
 *
 * <ul>
 *   <li><strong>Input:</strong> one header per line, as multimon-ng prints it ({@code EAS: ZCZC-…}) or bare;
 *       {@code NNNN} and other lines are ignored ({@link SameHeaderParser}).
 *   <li><strong>Repetitions:</strong> a SAME header is transmitted three times. An identical header received within
 *       {@value #REPEAT_SECONDS} s of the previous one for the same source, in the same request or an earlier one, is
 *       collapsed into it: counted as an item, no record.
 *   <li><strong>Record:</strong> externalId {@code sender|issue|event} (issue as ISO-8601 UTC minutes),
 *       {@code eventTime = observedAt} = the issue time, {@code validUntil} = issue + purge (asserted by the sender),
 *       payload {@code eventCode, eventName, org, locations, sender, rawHeader}, geometry the union of prompt 040's
 *       county polygons (the state's counties for {@code PSS000}); no geometry when none is known (marine areas, no
 *       reference data).
 *   <li>The target source must declare the kind {@value #KIND}; otherwise each header is rejected
 *       {@code UNKNOWN_KIND}.
 * </ul>
 */
@Component
public class SameIngestFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "same";

    /** The record kind. */
    public static final String KIND = "same-alert";

    /** The window within which an identical header is a repetition. */
    public static final int REPEAT_SECONDS = 10;

    /** The most remembered headers before old ones are dropped. */
    static final int MAX_REMEMBERED = 1_000;

    private final CountyGeometries counties;
    private final Map<String, Instant> lastSeen = new ConcurrentHashMap<>();

    /**
     * Creates the format.
     *
     * @param counties prompt 040's county outlines
     */
    @Autowired
    public SameIngestFormat(CountyGeometries counties) {
        this.counties = counties == null ? CountyGeometries.none() : counties;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.TEXT_PLAIN);
    }

    @Override
    public String description() {
        return "NOAA Weather Radio SAME headers, one per line (multimon-ng EAS output or bare ZCZC lines)";
    }

    @Override
    public boolean pathKeyFriendly() {
        return true;
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        ParsedIngest.Builder out = ParsedIngest.builder();
        String[] lines = request.bodyText().split("\\r?\\n");
        Instant receivedAt = request.receivedAt();
        prune(receivedAt);
        int index = 0;
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            int item = index++;
            out.items(item + 1);
            Optional<SameHeaderParser.Header> parsed = SameHeaderParser.parse(line);
            if (parsed.isEmpty()) {
                if (!SameHeaderParser.isEndOfMessage(line)) {
                    out.reject(item, null, "INVALID_ITEM", "not a SAME header");
                }
                continue;
            }
            SameHeaderParser.Header header = parsed.get();
            if (repetition(descriptor.id(), header, receivedAt)) {
                continue;
            }
            if (!descriptor.kinds().contains(KIND)) {
                out.reject(item, null, "UNKNOWN_KIND", "the source does not declare the kind " + KIND);
                continue;
            }
            Optional<Instant> issue = header.issue(receivedAt);
            if (issue.isEmpty()) {
                out.reject(item, null, "INVALID_ITEM", "day " + header.julianDay() + " does not exist");
                continue;
            }
            out.add(item, record(header, issue.get()));
        }
        return out.build();
    }

    /** The record of one header issued at {@code issue} (package-visible for tests). */
    NormalizedRecord record(SameHeaderParser.Header header, Instant issue) {
        String externalId = header.sender() + "|" + issue.truncatedTo(ChronoUnit.MINUTES) + "|" + header.eventCode();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventCode", header.eventCode());
        SameEventCodes.name(header.eventCode()).ifPresent(name -> payload.put("eventName", name));
        payload.put("org", header.org());
        payload.put("locations", header.locations());
        payload.put("sender", header.sender());
        payload.put("rawHeader", header.rawHeader());
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, KIND)
                .observedAt(issue)
                .eventTime(issue)
                .validUntil(issue.plus(header.purge()))
                .putAll(payload);
        geometry(header).ifPresent(b::geometry);
        return b.build();
    }

    /** The union of the named counties and whole states, or empty when none is known. */
    Optional<Geometry> geometry(SameHeaderParser.Header header) {
        List<Geometry> parts = new ArrayList<>();
        List<String> fips = header.countyFips();
        if (!fips.isEmpty()) {
            counties.union(fips).ifPresent(parts::add);
        }
        for (String state : header.wholeStates()) {
            counties.stateUnion(state).ifPresent(parts::add);
        }
        if (parts.isEmpty()) {
            return Optional.empty();
        }
        Geometry union = parts.getFirst();
        for (int i = 1; i < parts.size(); i++) {
            union = union.union(parts.get(i));
        }
        union.setSRID(4326);
        return Optional.of(union);
    }

    /** True when the same header reached this source within the repeat window; remembers it either way. */
    boolean repetition(String sourceId, SameHeaderParser.Header header, Instant receivedAt) {
        String key = sourceId + "\n" + header.rawHeader();
        Instant previous = lastSeen.put(key, receivedAt);
        return previous != null && !receivedAt.isBefore(previous)
                && Duration.between(previous, receivedAt).getSeconds() <= REPEAT_SECONDS;
    }

    private void prune(Instant now) {
        if (lastSeen.size() < MAX_REMEMBERED) {
            return;
        }
        Map<String, Instant> keep = new HashMap<>();
        lastSeen.forEach((k, v) -> {
            if (Duration.between(v, now).abs().getSeconds() <= REPEAT_SECONDS) {
                keep.put(k, v);
            }
        });
        lastSeen.clear();
        lastSeen.putAll(keep);
    }
}

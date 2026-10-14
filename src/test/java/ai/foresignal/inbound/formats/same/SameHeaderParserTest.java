package ai.foresignal.inbound.formats.same;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link SameHeaderParser} and the {@code same} format's parsing (prompt 086): multi-county headers, the whole-state
 * {@code 000} county, repetition collapse, the Julian-day year rollover, {@code NNNN} ignored, and the record shape.
 */
class SameHeaderParserTest {

    private static final Instant RECEIVED = Instant.parse("2026-10-13T15:04:20Z");
    private static final GeometryFactory GEOMETRY = new GeometryFactory();

    @Test
    @DisplayName("a multi-county header: originator, event, locations, purge, issue and sender")
    void multiCounty() {
        SameHeaderParser.Header h = SameHeaderParser.parse("EAS: ZCZC-WXR-TOR-048453-048201+0030-2861504-KEWX/NWS-")
                .orElseThrow();
        assertThat(h.org()).isEqualTo("WXR");
        assertThat(h.eventCode()).isEqualTo("TOR");
        assertThat(h.locations()).containsExactly("048453", "048201");
        assertThat(h.countyFips()).containsExactly("48453", "48201");
        assertThat(h.wholeStates()).isEmpty();
        assertThat(h.purge()).isEqualTo(Duration.ofMinutes(30));
        assertThat(h.sender()).isEqualTo("KEWX/NWS");
        assertThat(h.rawHeader()).isEqualTo("ZCZC-WXR-TOR-048453-048201+0030-2861504-KEWX/NWS-");
        assertThat(h.issue(RECEIVED)).contains(Instant.parse("2026-10-13T15:04:00Z"));
    }

    @Test
    @DisplayName("county 000 names the whole state; a part-of-county digit is kept in the location")
    void wholeState() {
        SameHeaderParser.Header h = SameHeaderParser.parse("ZCZC-CIV-CEM-048000-148453+0100-2861600-TXDPS/EM-")
                .orElseThrow();
        assertThat(h.wholeStates()).containsExactly("48");
        assertThat(h.countyFips()).containsExactly("48453");
        assertThat(h.locations()).containsExactly("048000", "148453");
        assertThat(h.purge()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    @DisplayName("Julian rollover: day 365 received on 1 January is last year; day 1 received on 31 December is next year")
    void rollover() {
        SameHeaderParser.Header late = SameHeaderParser.parse("ZCZC-WXR-WSW-048453+0045-3652330-KEWX/NWS-").orElseThrow();
        assertThat(late.issue(Instant.parse("2027-01-01T00:05:00Z"))).contains(Instant.parse("2026-12-31T23:30:00Z"));
        SameHeaderParser.Header early = SameHeaderParser.parse("ZCZC-WXR-WSW-048453+0045-0010005-KEWX/NWS-").orElseThrow();
        assertThat(early.issue(Instant.parse("2026-12-31T23:59:00Z"))).contains(Instant.parse("2027-01-01T00:05:00Z"));
        SameHeaderParser.Header leap = SameHeaderParser.parse("ZCZC-WXR-WSW-048453+0045-3662330-KEWX/NWS-").orElseThrow();
        assertThat(leap.issue(Instant.parse("2029-01-01T00:05:00Z"))).contains(Instant.parse("2028-12-31T23:30:00Z"));
    }

    @Test
    @DisplayName("NNNN, blank, malformed and out-of-range headers are not headers")
    void notHeaders() {
        assertThat(SameHeaderParser.parse("EAS: NNNN")).isEmpty();
        assertThat(SameHeaderParser.isEndOfMessage("EAS: NNNN")).isTrue();
        assertThat(SameHeaderParser.isEndOfMessage("NNNN")).isTrue();
        assertThat(SameHeaderParser.isEndOfMessage(null)).isFalse();
        assertThat(SameHeaderParser.parse("")).isEmpty();
        assertThat(SameHeaderParser.parse(null)).isEmpty();
        assertThat(SameHeaderParser.parse("ZCZC-WXR-TOR-48453+0030-2861504-KEWX/NWS-")).isEmpty();
        assertThat(SameHeaderParser.parse("ZCZC-WXR-TOR-048453+0060-2861504-KEWX/NWS-")).isEmpty();
        assertThat(SameHeaderParser.parse("ZCZC-WXR-TOR-048453+0030-3671504-KEWX/NWS-")).isEmpty();
        assertThat(SameHeaderParser.parse("ZCZC-WXR-TOR-048453+0030-2862504-KEWX/NWS-")).isEmpty();
        assertThat(SameEventCodes.name("TOR")).contains("Tornado Warning");
        assertThat(SameEventCodes.name("XYZ")).isEmpty();
        assertThat(SameEventCodes.all()).containsKeys("EAN", "RWT", "SQW");
    }

    /** County squares: Travis 48453, Harris 48201; Texas is their union. */
    static CountyGeometries counties() {
        return new CountyGeometries() {
            @Override
            public boolean available() {
                return true;
            }

            @Override
            public Optional<Geometry> union(Collection<String> fips5) {
                Geometry g = null;
                for (String f : fips5) {
                    Geometry square = f.equals("48453") ? box(-98.2, 30.0, -97.4, 30.6)
                            : f.equals("48201") ? box(-95.9, 29.5, -94.9, 30.2) : null;
                    if (square != null) {
                        g = g == null ? square : g.union(square);
                    }
                }
                return Optional.ofNullable(g);
            }

            @Override
            public Optional<Geometry> stateUnion(String stateFips2) {
                return stateFips2.equals("48") ? Optional.of(box(-106.6, 25.8, -93.5, 36.5)) : Optional.empty();
            }
        };
    }

    static Geometry box(double minX, double minY, double maxX, double maxY) {
        return GEOMETRY.createPolygon(new Coordinate[] {new Coordinate(minX, minY), new Coordinate(maxX, minY),
            new Coordinate(maxX, maxY), new Coordinate(minX, maxY), new Coordinate(minX, minY)});
    }

    static SourceDescriptor descriptor(String... kinds) {
        SourceDescriptor.Builder b = SourceDescriptor.builder("custom-nwr").name("NWR").mode(Mode.PUSH).custom(true)
                .defaultBudget(60, 86_400);
        for (String k : kinds) {
            b.kind(k);
        }
        return b.build();
    }

    static ParsedIngest parse(SameIngestFormat format, SourceDescriptor descriptor, String body, Instant at) {
        return format.parse(IngestRequest.of(body.getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN, at), descriptor,
                new FormatOptions(JsonMapper.builder().build().createObjectNode()));
    }

    @Test
    @DisplayName("the fixture: three repetitions collapse to one record, NNNN ignored, county and state geometry")
    void fixture() {
        SameIngestFormat format = new SameIngestFormat(counties());
        ParsedIngest parsed = parse(format, descriptor(SameIngestFormat.KIND),
                AdapterFixtures.text("local-rf", "same-headers.txt"), RECEIVED);
        assertThat(parsed.rejections()).isEmpty();
        assertThat(parsed.records()).hasSize(3);
        NormalizedRecord tor = parsed.records().getFirst();
        assertThat(tor.externalId()).isEqualTo("KEWX/NWS|2026-10-13T15:04:00Z|TOR");
        assertThat(tor.kind()).isEqualTo(SameIngestFormat.KIND);
        assertThat(tor.eventTime()).isEqualTo(Instant.parse("2026-10-13T15:04:00Z"));
        assertThat(tor.observedAt()).isEqualTo(tor.eventTime());
        assertThat(tor.validUntil()).isEqualTo(Instant.parse("2026-10-13T15:34:00Z"));
        assertThat(tor.payload()).containsEntry("eventCode", "TOR").containsEntry("eventName", "Tornado Warning")
                .containsEntry("org", "WXR").containsEntry("sender", "KEWX/NWS")
                .containsEntry("locations", List.of("048453", "048201"))
                .containsEntry("rawHeader", "ZCZC-WXR-TOR-048453-048201+0030-2861504-KEWX/NWS-");
        assertThat(tor.geometry().getNumGeometries()).isEqualTo(2);
        Envelope state = parsed.records().get(1).geometry().getEnvelopeInternal();
        assertThat(state.getMinX()).isEqualTo(-106.6);
        NormalizedRecord wsw = parsed.records().get(2);
        assertThat(wsw.validUntil()).as("the purge rolls into the next year")
                .isEqualTo(Instant.parse("2027-01-01T00:15:00Z"));
    }

    @Test
    @DisplayName("a repetition in a later request within 10 s is collapsed; after 10 s it is a record again")
    void repetitionAcrossRequests() {
        SameIngestFormat format = new SameIngestFormat(CountyGeometries.none());
        SourceDescriptor d = descriptor(SameIngestFormat.KIND);
        String header = "ZCZC-WXR-SVR-048453+0030-2861504-KEWX/NWS-";
        assertThat(parse(format, d, header, RECEIVED).records()).hasSize(1);
        ParsedIngest repeat = parse(format, d, header, RECEIVED.plusSeconds(1));
        assertThat(repeat.records()).isEmpty();
        assertThat(repeat.items()).isEqualTo(1);
        assertThat(parse(format, d, header, RECEIVED.plusSeconds(30)).records()).hasSize(1);
        assertThat(parse(format, d, header, RECEIVED.plusSeconds(30)).records().isEmpty()).isTrue();
        NormalizedRecord r = parse(format, d, header, RECEIVED.plusSeconds(60)).records().getFirst();
        assertThat(r.geometry()).as("no reference data, no geometry").isNull();
    }

    @Test
    @DisplayName("a source without the same-alert kind rejects headers; other lines are invalid items")
    void rejections() {
        SameIngestFormat format = new SameIngestFormat(null);
        ParsedIngest wrongKind = parse(format, descriptor("other"), "ZCZC-WXR-SVR-048453+0030-2861504-KEWX/NWS-", RECEIVED);
        assertThat(wrongKind.rejections()).singleElement().satisfies(r -> assertThat(r.reason()).isEqualTo("UNKNOWN_KIND"));
        ParsedIngest junk = parse(format, descriptor(SameIngestFormat.KIND), "hello\n\nNNNN\n", RECEIVED);
        assertThat(junk.rejections()).singleElement().satisfies(r -> assertThat(r.reason()).isEqualTo("INVALID_ITEM"));
        assertThat(junk.items()).isEqualTo(2);
        ParsedIngest badDay = parse(format, descriptor(SameIngestFormat.KIND),
                "ZCZC-WXR-SVR-048453+0030-3661504-KEWX/NWS-", Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(badDay.rejections()).singleElement().satisfies(r -> assertThat(r.reason()).isEqualTo("INVALID_ITEM"));
        assertThat(format.id()).isEqualTo("same");
        assertThat(format.mediaTypes()).containsExactly(MediaType.TEXT_PLAIN);
        assertThat(format.pathKeyFriendly()).isTrue();
        assertThat(format.description()).contains("SAME");
    }
}

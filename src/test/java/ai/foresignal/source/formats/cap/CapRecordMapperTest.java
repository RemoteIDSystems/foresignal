package ai.foresignal.source.formats.cap;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

/** {@link CapRecordMapper} (prompt 067): the alert time mapping, state, info choice, text caps and parameters. */
class CapRecordMapperTest {

    private static final CapMappingProfile PROFILE = CapMappingProfile.builder("cap-alert")
            .parameters("VTEC").build();

    private static String text(String name) {
        return new String(AdapterFixtures.bytes("cap", name), StandardCharsets.UTF_8);
    }

    private static CapAlert parse(String xml) {
        return CapParser.parse(xml.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("observedAt = sent, eventTime = onset, validFrom = onset, validUntil = expires; the payload")
    void timeBase() {
        NormalizedRecord record = CapRecordMapper.toRecord(parse(text("polygon.xml")), PROFILE);

        assertThat(record.externalId()).as("own {sender}:{identifier}").isEqualTo("alerts@example.org:POLY-1");
        assertThat(record.kind()).isEqualTo("cap-alert");
        assertThat(record.tombstone()).isFalse();
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(record.eventTime()).isEqualTo(Instant.parse("2026-10-13T13:00:00Z"));
        assertThat(record.validFrom()).isEqualTo(Instant.parse("2026-10-13T13:00:00Z"));
        assertThat(record.validUntil()).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));
        assertThat(record.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(record.payload())
                .containsEntry("identifier", "POLY-1")
                .containsEntry("sender", "alerts@example.org")
                .containsEntry("sent", "2026-10-13T12:00:00Z")
                .containsEntry("msgType", "Alert")
                .containsEntry("status", "Actual")
                .containsEntry("scope", "Public")
                .containsEntry("state", "active")
                .containsEntry("event", "Flood Warning")
                .containsEntry("category", List.of("Met"))
                .containsEntry("urgency", "Immediate")
                .containsEntry("severity", "Severe")
                .containsEntry("certainty", "Likely")
                .containsEntry("headline", "Flood Warning for the valley")
                .containsEntry("areaDesc", "Valley; Open ring")
                .containsEntry("geocodes", Map.of("SAME", List.of("048453")))
                .containsEntry("onset", "2026-10-13T13:00:00Z")
                .containsEntry("effective", "2026-10-13T12:00:00Z")
                .containsEntry("expires", "2026-10-13T18:00:00Z")
                .containsEntry("language", "en-US")
                .containsEntry("references", 0);
    }

    @Test
    @DisplayName("validFrom falls back to effective, then null; validFrom after validUntil is left out")
    void validFromFallbacks() {
        String noOnset = text("polygon.xml").replace("<onset>2026-10-13T13:00:00-00:00</onset>", "");
        NormalizedRecord effective = CapRecordMapper.toRecord(parse(noOnset), PROFILE);
        assertThat(effective.eventTime()).isNull();
        assertThat(effective.validFrom()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));

        String neither = noOnset.replace("<effective>2026-10-13T12:00:00-00:00</effective>", "");
        assertThat(CapRecordMapper.toRecord(parse(neither), PROFILE).validFrom()).isNull();

        String inverted = text("polygon.xml").replace("<onset>2026-10-13T13:00:00-00:00</onset>",
                "<onset>2026-10-13T20:00:00-00:00</onset>");
        NormalizedRecord record = CapRecordMapper.toRecord(parse(inverted), PROFILE);
        assertThat(record.validFrom()).isNull();
        assertThat(record.validUntil()).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));
        assertThat(record.eventTime()).isEqualTo(Instant.parse("2026-10-13T20:00:00Z"));
    }

    @Test
    @DisplayName("validUntil is expires; no expires means no validUntil")
    void validUntil() {
        String noExpires = text("polygon.xml").replace("<expires>2026-10-13T18:00:00-00:00</expires>", "");
        assertThat(CapRecordMapper.toRecord(parse(noExpires), PROFILE).validUntil()).isNull();
    }

    @Test
    @DisplayName("a Cancel without info is a canceled version (not a tombstone) with validUntil = sent")
    void cancelWithoutInfo() {
        CapAlert alert = parse(text("cancel-no-info.xml"));

        NormalizedRecord record = CapRecordMapper.toRecord(alert, "thread-1", PROFILE);

        assertThat(record.tombstone()).isFalse();
        assertThat(record.externalId()).isEqualTo("thread-1");
        assertThat(record.payload()).containsEntry("state", "cancelled").containsEntry("references", 1)
                .doesNotContainKeys("event", "headline");
        assertThat(record.validUntil()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(record.geometry()).isNull();
    }

    @Test
    @DisplayName("a Cancel with info expires at info.expires; an Update is updated; a profile rule can cancel")
    void states() {
        String cancel = text("polygon.xml").replace("<msgType>Alert</msgType>", "<msgType>Cancel</msgType>");
        NormalizedRecord canceled = CapRecordMapper.toRecord(parse(cancel), PROFILE);
        assertThat(canceled.payload()).containsEntry("state", "cancelled");
        assertThat(canceled.validUntil()).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));

        NormalizedRecord updated = CapRecordMapper.toRecord(parse(text("references.xml")), PROFILE);
        assertThat(updated.payload()).containsEntry("state", "updated").containsEntry("references", 2);

        CapMappingProfile rule = CapMappingProfile.builder("cap-alert")
                .cancellation((alert, info) -> info != null && "Flood Warning".equals(info.event())).build();
        NormalizedRecord byRule = CapRecordMapper.toRecord(parse(text("references.xml")), rule);
        assertThat(byRule.payload()).containsEntry("state", "cancelled");
        assertThat(byRule.validUntil()).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));
    }

    @Test
    @DisplayName("the info block is chosen by the language list (exact, then primary subtag), else the first")
    void languageSelection() {
        CapAlert alert = parse(text("multi-language.xml"));

        assertThat(CapRecordMapper.toRecord(alert, CapMappingProfile.of("x")).payload())
                .containsEntry("event", "Avertissement de pluie");
        assertThat(CapRecordMapper.toRecord(alert, CapMappingProfile.builder("x").languages("en-CA").build())
                .payload()).containsEntry("event", "Rainfall Warning");
        assertThat(CapRecordMapper.toRecord(alert, CapMappingProfile.builder("x").languages("en-US", "en").build())
                .payload()).containsEntry("event", "Rainfall Warning").containsEntry("language", "en-CA");
        assertThat(CapRecordMapper.chooseInfo(alert, List.of("de")).language()).isEqualTo("fr-CA");
    }

    @Test
    @DisplayName("headline, description and instruction are capped at the profile limit with an ellipsis")
    void textCaps() {
        String longText = "é".repeat(5000);
        String xml = text("polygon.xml").replace("River flooding is expected.", longText);

        NormalizedRecord record = CapRecordMapper.toRecord(parse(xml), PROFILE);

        String description = (String) record.payload().get("description");
        assertThat(description.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(4096);
        assertThat(description).endsWith("…");
        assertThat(CapRecordMapper.truncate("short", 16)).isEqualTo("short");
        assertThat(CapRecordMapper.truncate("   ", 16)).isNull();
        NormalizedRecord small = CapRecordMapper.toRecord(parse(xml),
                CapMappingProfile.builder("x").textMaxBytes(64).build());
        assertThat(((String) small.payload().get("description")).getBytes(StandardCharsets.UTF_8).length)
                .isLessThanOrEqualTo(64);
    }

    @Test
    @DisplayName("only allowlisted parameters reach the payload; none without an allowlist")
    void parameterAllowlist() {
        CapAlert alert = parse(text("polygon.xml"));

        assertThat(CapRecordMapper.toRecord(alert, PROFILE).payload())
                .containsEntry("parameters", Map.of("VTEC", List.of("/O.NEW.KEWX.FL.W.0001.261013T1300Z-261013T1800Z/")));
        assertThat(CapRecordMapper.toRecord(alert, CapMappingProfile.of("x")).payload()).doesNotContainKey("parameters");
    }

    @Test
    @DisplayName("the extender adds keys and may replace the geometry")
    void extender() {
        CapMappingProfile profile = CapMappingProfile.builder("x")
                .extender((alert, info, geometry, record) -> {
                    record.put("extra", geometry == null ? "none" : geometry.getGeometryType());
                    record.point(1, 2);
                }).build();

        NormalizedRecord record = CapRecordMapper.toRecord(parse(text("geocode-only.xml")), profile);

        assertThat(record.payload()).containsEntry("extra", "none");
        assertThat(record.geometry()).isInstanceOf(Point.class);
    }

    @Test
    @DisplayName("the embedded fields are headline, description and instruction")
    void embedFields() {
        assertThat(CapRecordMapper.EMBED_FIELDS).containsExactly("headline", "description", "instruction");
    }
}

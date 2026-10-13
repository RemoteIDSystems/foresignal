package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.CollectorProperties;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.PolicyAwareRecordSink;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code satdump-itest} end to end (prompt 049): the station's loops through the real stream context
 * (inbox, media store, raster pipeline, adapter state) and the real ingest writer. A pass goes
 * {@code receiving → recorded → decoded} as one record with its composite and thumbnails as media; HRIT
 * folders become dynamic products with {@code raster-frame} records; and the "Meteor pass decoded" rule
 * ({@code kind=satellite-pass}, {@code status == decoded}, {@code enter}) fires once.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class SatDumpIngestIT {

    static final String SOURCE = "satdump-" + RasterTestSources.SATDUMP_STATION;
    static final Instant AOS = Instant.parse("2026-10-12T18:01:50Z");

    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired FetchContextFactory contexts;
    @Autowired IngestQueue queue;
    @Autowired CollectorProperties collector;
    @Autowired ForesignalProperties properties;
    @Autowired MeterRegistry meters;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProductRegistry products;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;

    Path station;
    String inboxPath;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(AOS.minusSeconds(10));
        inboxPath = "satdump/itest-" + UUID.randomUUID();
        station = Files.createDirectories(properties.inboxDir().resolve(inboxPath));
        sources.update(SOURCE, new SourceSettingsUpdate(false, null, Map.of(SatDumpDescriptor.INBOX_PATH, inboxPath,
                SatDumpDescriptor.LOCATION, "29.76,-95.37"), Budget.of(600, 100_000), null, false, null, false));
    }

    /** A sink like the streaming supervisor's, feeding the real ingest queue. */
    PolicyAwareRecordSink sink() {
        PolicyAwareRecordSink sink = new PolicyAwareRecordSink(registry.descriptor(SOURCE), IngestPolicy.PolicyMode.ALL,
                queue::submit, new PolicyAwareRecordSink.FlushListener() {
                    @Override
                    public void flushed(FetchLogEntry log, int records, IngestOutcome outcome) {}

                    @Override
                    public void failed(FetchLogEntry log, int records, Throwable error) {}
                }, clock, collector.stream(), "test", meters);
        sink.start();
        return sink;
    }

    SatDumpAdapter adapter() {
        return (SatDumpAdapter) registry.get(SOURCE).adapterIfPresent().orElseThrow();
    }

    StreamContext context() {
        return contexts.forStream(registry.descriptor(SOURCE), null, null);
    }

    int versions(String kind, String externalId) {
        Integer n = jdbc.queryForObject("select count(*) from record_version where source_id = ? and kind = ?"
                + " and external_id = ?", Integer.class, SOURCE, kind, externalId);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("receiving → recorded → decoded in one pass record, composite and thumbnails as media; the rule fires once")
    void passLifecycle() throws Exception {
        UUID rule = rules.create(JsonMapper.builder().build().readTree("{\"name\":\"Meteor pass decoded\","
                + "\"kinds\":[\"satellite-pass\"],\"conditions\":[{\"path\":\"status\",\"op\":\"==\","
                + "\"value\":\"decoded\"}],\"mode\":\"enter\"}")).rule().getId();
        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = adapter().newStation(context(), sink);

        ApiSnapshot pass = SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack.json"));
        ApiSnapshot idle = SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack-idle.json"));
        clock.setInstant(AOS);
        s.observe(pass, AOS).forEach(sink::accept);
        for (int i = 1; i <= 12; i++) {
            clock.setInstant(AOS.plusSeconds(5L * i));
            s.observe(pass, clock.instant()).forEach(sink::accept);
        }
        clock.setInstant(AOS.plusSeconds(65));
        s.observe(idle, clock.instant()).forEach(sink::accept);

        SatDumpFixtures.copy(SatDumpFixtures.PASS, station.resolve("2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz"));
        for (int i = 0; i < 3; i++) {
            clock.setInstant(AOS.plusSeconds(120 + 30L * i));
            s.scanInboxOnce();
        }
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();

        String passId = "itest:meteor-m2-3:" + AOS.getEpochSecond() / 60;
        assertThat(versions(PassBook.KIND, passId)).isEqualTo(3);
        List<String> statuses = jdbc.queryForList("select payload->>'status' from record_version where source_id = ?"
                + " and external_id = ? order by seq", String.class, SOURCE, passId);
        assertThat(statuses).containsExactly("receiving", "recorded", "decoded");
        String rf = jdbc.queryForObject("select payload->'rf'->>'syncedFraction' from record_head where source_id = ?"
                + " and external_id = ?", String.class, SOURCE, passId);
        assertThat(rf).isEqualTo("1.0");
        Integer media = jdbc.queryForObject("select count(*) from media where record_source_id = ? and record_external_id"
                + " = ?", Integer.class, SOURCE, passId);
        assertThat(media).isGreaterThanOrEqualTo(3);
        Integer thumbs = jdbc.queryForObject("select count(*) from media where record_external_id = ? and role ="
                + " 'thumbnail'", Integer.class, passId);
        assertThat(thumbs).isEqualTo(2);
        // locked at AOS, idle at LOS: the quantized payload did not change in between.
        assertThat(versions(GroundStationRecords.KIND, "itest")).isEqualTo(2);

        engine.catchUp();
        Integer alerts = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        assertThat(alerts).isEqualTo(1);
    }

    @Test
    @DisplayName("HRIT folders become dynamic products with raster-frame records once complete")
    void hritFrames() throws Exception {
        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = adapter().newStation(context(), sink);
        SatDumpFixtures.copy("xrit", station);

        clock.setInstant(Instant.parse("2026-10-12T18:01:00Z"));
        s.scanInboxOnce();
        String id = "satdump-itest-goes-19-full-disk-13";
        assertThat(products.find(id)).hasValueSatisfying(p -> {
            assertThat(p.dynamic()).isTrue();
            assertThat(p.sourceId()).isEqualTo(SOURCE);
            assertThat(p.spec().spatial().coverage()).isEqualTo("station-disc");
        });
        assertThat(jdbc.queryForObject("select status from raster_frame where product_id = ?", String.class, id))
                .isEqualTo("partial");

        clock.setInstant(Instant.parse("2026-10-12T18:03:00Z"));
        s.scanInboxOnce();
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();
        assertThat(jdbc.queryForObject("select status from raster_frame where product_id = ?", String.class, id))
                .isEqualTo("complete");
        assertThat(versions("raster-frame", id)).isEqualTo(1);
        assertThat(versions("raster-frame", "satdump-itest-goes-19-full-disk-2")).isEqualTo(1);
        products.removeDynamic(id);
        products.removeDynamic("satdump-itest-goes-19-full-disk-2");
    }

    @Test
    @DisplayName("without apiUrl the station starts with one unknown ground-station record")
    void unknownWithoutApi() throws Exception {
        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = adapter().newStation(context(), sink);
        s.start();
        s.close();
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();
        assertThat(jdbc.queryForObject("select payload->>'state' from record_version where source_id = ? and kind ="
                + " 'ground-station'", String.class, SOURCE)).isEqualTo("unknown");
        assertThat(sink.flushes()).isGreaterThanOrEqualTo(1);
    }
}

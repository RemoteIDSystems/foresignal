package ai.foresignal.ical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

/**
 * iCalendar feeds end to end (prompt 112): launches, TFRs, NWS alerts and a custom kind from fixture records,
 * canceled records, briefing expansion across a daylight-saving change, {@code ICAL_FIELDS_MISSING}, the ETag
 * and a feed-key subscription.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class CalendarFeedIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** A custom source declaring interval fields. */
    static final String INTERVALS = "test-ical-intervals";

    /** A custom source without interval fields. */
    static final String PLAIN = "test-ical-plain";

    @Autowired BriefingService briefings;
    @Autowired FeedKeyService feedKeys;

    @BeforeEach
    void register() {
        unregister();
        registry.registerDynamic(SourceDescriptor.builder(INTERVALS).name("Intervals").kind("ical-window")
                .mode(Mode.PUSH).historyTier(HistoryTier.STANDARD).defaultBudget(Budget.of(60, 10_000))
                .licence("CC BY 4.0").attribution("Data: intervals").intervalFields("opens", "closes").build());
        registry.registerDynamic(SourceDescriptor.builder(PLAIN).name("Plain").kind("ical-plain").mode(Mode.PUSH)
                .historyTier(HistoryTier.STANDARD).defaultBudget(Budget.of(60, 10_000)).licence("CC BY 4.0")
                .attribution("Data: plain").build());
    }

    @AfterEach
    void unregister() {
        for (String id : List.of(INTERVALS, PLAIN)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    String ics(String feed, String query) throws Exception {
        MvcResult result = mvc.perform(get("/v1/calendars/" + feed + ".ics" + query)).andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/calendar;charset=UTF-8")).andReturn();
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** The unfolded content lines. */
    static List<String> lines(String ics) {
        return Arrays.asList(ics.replace("\r\n ", "").split("\r\n"));
    }

    @Test
    @DisplayName("launches: NET, window end, Go is CONFIRMED, pad location and GEO, stable UID and SEQUENCE")
    void launches() throws Exception {
        Instant net = T0.plus(Duration.ofDays(2));
        ingestAt("launch-library", T0, NormalizedRecord.builder("ll-1", "launch").observedAt(T0).point(-80.6, 28.6)
                .put("name", "Falcon 9 | Demo").put("net", net.toString()).put("windowEnd", net.plusSeconds(7200).toString())
                .put("status", "Go").put("padName", "LC-39A").build());
        ingestAt("launch-library", T0.plusSeconds(1), NormalizedRecord.builder("ll-2", "launch").observedAt(T0)
                .point(-120.6, 34.7).put("name", "Electron").put("net", net.toString()).put("status", "TBD").build());
        String ics = ics("launches", "");
        List<String> lines = lines(ics);
        assertThat(lines).contains("BEGIN:VCALENDAR", "PRODID:-//Foresignal//" + versionOf(lines) + "//EN",
                "REFRESH-INTERVAL;VALUE=DURATION:PT15M", "DTSTART:" + ICalWriter.time(net),
                "DTEND:" + ICalWriter.time(net.plusSeconds(7200)), "DTEND:" + ICalWriter.time(net.plusSeconds(3600)),
                "STATUS:CONFIRMED", "STATUS:TENTATIVE", "LOCATION:LC-39A", "SEQUENCE:1");
        String instance = lines.stream().filter(l -> l.startsWith("UID:launch-library:ll-1@")).findFirst().orElseThrow();
        assertThat(ics("launches", "")).contains(instance);
        assertThat(lines).anyMatch(l -> l.startsWith("GEO:28.6"));
        assertThat(lines).anyMatch(l -> l.startsWith("DESCRIPTION:") && l.contains("Source: launch-library"));
    }

    static String versionOf(List<String> lines) {
        String prodid = lines.stream().filter(l -> l.startsWith("PRODID:")).findFirst().orElseThrow();
        return prodid.substring("PRODID:-//Foresignal//".length(), prodid.length() - "//EN".length());
    }

    @Test
    @DisplayName("a tombstoned record stays as CANCELLED; SEQUENCE follows the record version")
    void canceled() throws Exception {
        Instant net = T0.plus(Duration.ofDays(3));
        ingestAt("launch-library", T0, NormalizedRecord.builder("ll-x", "launch").observedAt(T0).point(-80.6, 28.6)
                .put("name", "Scrubbed").put("net", net.toString()).put("status", "Go").build());
        ingestAt("launch-library", T0.plusSeconds(60), NormalizedRecord.builder("ll-x", "launch")
                .observedAt(T0.plusSeconds(60)).tombstone(true).build());
        List<String> lines = lines(ics("launches", ""));
        assertThat(lines).contains("STATUS:CANCELLED", "SEQUENCE:2");
        clock.setInstant(T0.plus(Duration.ofDays(8)));
        assertThat(lines(ics("launches", ""))).noneMatch(l -> l.startsWith("UID:launch-library:ll-x@"));
    }

    @Test
    @DisplayName("TFRs: effective to expiry, open-ended ones DTSTART only with X-FORESIGNAL-OPEN-ENDED")
    void tfr() throws Exception {
        ingestAt("faa-tfr", T0, NormalizedRecord.builder("tfr-1", "airspace-restriction").observedAt(T0)
                .point(-97.7, 30.2).validFrom(T0.plusSeconds(3600)).validUntil(T0.plus(Duration.ofDays(1)))
                .put("notamId", "1/2345").build());
        ingestAt("faa-tfr", T0.plusSeconds(1), NormalizedRecord.builder("tfr-2", "airspace-restriction").observedAt(T0)
                .point(-97.7, 30.2).validFrom(T0).put("notamId", "1/2346").build());
        List<String> lines = lines(ics("tfr", ""));
        assertThat(lines).contains("DTSTART:" + ICalWriter.time(T0.plusSeconds(3600)),
                "DTEND:" + ICalWriter.time(T0.plus(Duration.ofDays(1))), "X-FORESIGNAL-OPEN-ENDED:TRUE");
    }

    @Test
    @DisplayName("NWS: onset to ends, SUMMARY is the event and the area")
    void nws() throws Exception {
        Instant onset = T0.plusSeconds(1800);
        ingestAt("nws-alerts", T0, NormalizedRecord.builder("urn:oid:2.49.0.1.840.0.1", "nws-alert").observedAt(T0)
                .point(-97.7, 30.2).validFrom(onset).validUntil(T0.plus(Duration.ofHours(6)))
                .put("event", "Flood Warning").put("areaDesc", "Travis, TX").put("ends", T0.plus(Duration.ofHours(5)).toString())
                .build());
        List<String> lines = lines(ics("nws", ""));
        assertThat(lines).contains("DTSTART:" + ICalWriter.time(onset), "DTEND:" + ICalWriter.time(T0.plus(Duration.ofHours(5))),
                "SUMMARY:Flood Warning — Travis\\, TX");
    }

    @Test
    @DisplayName("custom: a kind with startField/endField; one without is 400 ICAL_FIELDS_MISSING")
    void custom() throws Exception {
        ingestAt(INTERVALS, T0, NormalizedRecord.builder("w1", "ical-window").observedAt(T0).point(1, 2)
                .put("title", "Window").put("opens", T0.plusSeconds(60).toString()).put("closes", T0.plusSeconds(120).toString())
                .build());
        List<String> lines = lines(ics("custom", "?source=" + INTERVALS + "&kind=ical-window"));
        assertThat(lines).contains("DTSTART:" + ICalWriter.time(T0.plusSeconds(60)),
                "DTEND:" + ICalWriter.time(T0.plusSeconds(120)));
        MvcResult missing = mvc.perform(get("/v1/calendars/custom.ics?source=" + PLAIN + "&kind=ical-plain"))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(JSON.readTree(missing.getResponse().getContentAsString()).get("code").asString())
                .isEqualTo("ICAL_FIELDS_MISSING");
        mvc.perform(get("/v1/calendars/custom.ics")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/calendars/nope.ics")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("briefings expand to individual events in local time across a DST change, never an RRULE")
    void briefings() throws Exception {
        clock.setInstant(Instant.parse("2026-10-25T00:00:00Z"));
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "test-admin", Scope.ADMIN, null, null,
                null, false);
        briefings.create(JSON.readTree("{\"name\":\"Morning\",\"schedule\":\"0 7 * * *\",\"timezone\":\"America/Chicago\","
                + "\"scope\":{\"kinds\":[\"launch\"]},\"lookback\":\"24h\"}"),
                new McpCaller(token, new ReadCaller(Scope.ADMIN, false, true), Set.of(), "127.0.0.1"));
        List<String> lines = lines(ics("briefings", ""));
        assertThat(lines.stream().filter(l -> l.equals("BEGIN:VEVENT")).count()).isEqualTo(30);
        assertThat(lines).contains("DTSTART:20261031T120000Z", "DTSTART:20261101T130000Z").noneMatch(l -> l.startsWith("RRULE"));
        assertThat(lines).anyMatch(l -> l.startsWith("SUMMARY:Briefing: Morning"));
    }

    @Test
    @DisplayName("an ETag answers If-None-Match with 304; a feed key subscribes with openReads off")
    void etagAndFeedKey() throws Exception {
        ingestAt("launch-library", T0, NormalizedRecord.builder("ll-k", "launch").observedAt(T0).point(-80.6, 28.6)
                .put("name", "Keyed").put("net", T0.plusSeconds(3600).toString()).build());
        MvcResult first = mvc.perform(get("/v1/calendars/launches.ics")).andExpect(status().isOk()).andReturn();
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        mvc.perform(get("/v1/calendars/launches.ics").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        FeedKeyService.CreatedKey key = feedKeys.create(JSON.readTree("{\"name\":\"calendar\",\"prefixes\":"
                + "[\"/v1/calendars\"],\"expiresAt\":\"" + T0.plus(Duration.ofDays(30)) + "\"}"));
        mvc.perform(get("/v1/calendars/launches.ics")).andExpect(status().isUnauthorized());
        String keyed = mvc.perform(get("/v1/k/" + key.key() + "/calendars/launches.ics")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(keyed).contains("UID:launch-library:ll-k@");
    }
}

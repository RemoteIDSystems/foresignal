package ai.foresignal.weather;

import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.briefing.BriefingPrompts;
import ai.foresignal.briefing.BriefingSection;
import ai.foresignal.briefing.BriefingSectionContributor;
import ai.foresignal.briefing.DigestService;
import ai.foresignal.briefing.DigestStats;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.Scope;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * "Weather at places" in briefings and digests (prompt 056).
 *
 * <ul>
 *   <li>The section itself, built on in-memory records for two places in different timezones (America/Chicago and
 *       Asia/Tokyo): now, period and next parts; stale and unavailable parts labeled, never omitted; the
 *       partial-period note below the retention floor; and the golden markdown table.
 *   <li>The wiring, on the briefing test context with real places: LLM-mode briefings receive the section as a data
 *       block with the "unknown is not clear" instruction, {@code /v1/digest?places=} includes {@code weatherAtPlaces}
 *       (401 without a token, 400 for an unknown place), and unknown {@code scope.places} or
 *       {@code scope.weatherProducts} ids are refused.
 * </ul>
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class BriefingWeatherSectionIT extends BriefingItSupport {

    static final Instant NOW = Instant.parse("2026-10-12T12:00:00Z");
    static final Instant FROM = Instant.parse("2026-10-11T12:00:00Z");

    @Autowired PlaceService placeService;
    @Autowired DigestService digests;

    @AfterEach
    void cleanPlaces() {
        for (String id : List.of("wx-home", "wx-tokyo")) {
            if (placeService.find(id).isPresent()) {
                placeService.delete(id, true);
            }
        }
        retentionState.reset();
    }

    // ------------------------------------------------------------------ the section on in-memory records

    static PlaceView place(String id, String name, double lon, double lat, String zone) {
        return new PlaceView(id, name, "point", Geo.point(lon, lat), 20.0, null, null, zone, null, List.of(), List.of(),
                Map.of(), false, 1, NOW, NOW);
    }

    static boolean isAustin(Geometry g) {
        return g.getEnvelopeInternal().contains(-97.75, 30.27);
    }

    /** Records for the two places. */
    static final class Records implements WeatherRecords {

        @Override
        public List<Row> nearby(String sourceId, String kind, Geometry geometry, double radiusKm, int limit) {
            return List.of();
        }

        @Override
        public List<Row> intersecting(String sourceId, String kind, Geometry geometry, Instant at, int limit) {
            if (!isAustin(geometry)) {
                return List.of();
            }
            return List.of(new Row("nws-alerts", "urn:1", "nws-alert", 5, NOW, Map.of("event",
                    "Severe Thunderstorm Warning", "severity", "Severe", "expires", "2026-10-12T13:45:00Z"), null, null));
        }

        @Override
        public Optional<Row> head(String sourceId, String externalId) {
            if ("wx-places".equals(sourceId) && "place:wx-home".equals(externalId)) {
                return Optional.of(new Row(sourceId, externalId, "place-weather", 50, NOW, Map.of(
                        "radar", Map.of("status", "ok", "maxDbzInRadius", 52.5, "nearest50Km", 14.0),
                        "lightning", Map.of("status", "stale", "asOf", "2026-10-12T11:30:00Z", "statusText",
                                WeatherSections.STALE_TEXT, "nearestKm15m", 6.0, "count15km15m", 3)), null, null));
            }
            if ("wx-places".equals(sourceId) && "place:wx-tokyo".equals(externalId)) {
                return Optional.of(new Row(sourceId, externalId, "place-weather", 51, NOW, Map.of(
                        "radar", Map.of("status", "no-coverage", "statusText", WeatherSections.NO_COVERAGE_TEXT),
                        "lightning", Map.of("status", "ok", "nearestKm15m", 999, "count15km15m", 0)), null, null));
            }
            if ("nws-gridpoints".equals(sourceId) && "place:wx-home".equals(externalId)) {
                return Optional.of(new Row(sourceId, externalId, "nws-forecast", 52, NOW, Map.of("popMax24h", 70,
                        "qpf24hMm", 12.5, "gustMaxMs24h", 14.2, "tMaxC24h", 31.0, "tMinC24h", 18.0, "forecastUpdated",
                        "2026-10-12T11:00:00Z"), null, null));
            }
            return Optional.empty();
        }

        @Override
        public List<Row> versions(String sourceId, String externalId, long fromSeq, long toSeq, int limit) {
            if (!"place:wx-home".equals(externalId)) {
                return List.of();
            }
            return List.of(
                    new Row(sourceId, externalId, "place-weather", 90, FROM, Map.of(
                            "radar", Map.of("status", "ok", "maxDbzInRadius", 40.0),
                            "hail", Map.of("status", "ok", "meshMaxMmInRadius", 5)), null, null),
                    new Row(sourceId, externalId, "place-weather", 120, NOW, Map.of(
                            "radar", Map.of("status", "ok", "maxDbzInRadius", 55.0),
                            "hail", Map.of("status", "ok", "meshMaxMmInRadius", 19)), null, null),
                    new Row(sourceId, externalId, "place-weather", 130, NOW, Map.of(
                            "radar", Map.of("status", "stale", "maxDbzInRadius", 60.0)), null, null));
        }

        @Override
        public List<Row> entered(List<String> sources, List<String> kinds, Geometry area, long fromSeq, long toSeq,
                int limit) {
            if (!isAustin(area)) {
                return List.of();
            }
            return List.of(new Row("mrms-probsevere", "ps-1", "probsevere-object", 110,
                    Instant.parse("2026-10-12T10:15:00Z"), Map.of(), null, null));
        }
    }

    @SuppressWarnings("unchecked")
    WeatherAtPlacesSection section() {
        PlaceService places = mock(PlaceService.class);
        when(places.find("wx-home")).thenReturn(Optional.of(place("wx-home", "Home", -97.75, 30.27, "America/Chicago")));
        when(places.find("wx-tokyo")).thenReturn(Optional.of(place("wx-tokyo", "Tokyo", 139.7, 35.6, "Asia/Tokyo")));
        ForecastSeriesService forecasts = mock(ForecastSeriesService.class);
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of("time", "2026-10-12T15:00:00Z", "gustMs", 12.0, "precipMm", 10.0));
        items.add(Map.of("time", "2026-10-12T23:00:00Z", "gustMs", 21.0, "precipMm", 20.0));
        items.add(Map.of("time", "2026-10-16T00:00:00Z", "gustMs", 40.0, "precipMm", 99.0));
        when(forecasts.series(eq("gfs"), anyDouble(), anyDouble())).thenReturn(Optional.of(Map.of("provider", "gfs",
                "status", "ok", "run", "2026-10-12T06:00:00Z", "items", items)));
        when(forecasts.series(eq("hrrr"), anyDouble(), anyDouble())).thenReturn(Optional.empty());
        RetentionState retention = mock(RetentionState.class);
        when(retention.lowestRetainedSeq(anySet())).thenReturn(150L);
        ObjectProvider<FlashStore> flashes = mock(ObjectProvider.class);
        return new WeatherAtPlacesSection(places, new Records(), forecasts, retention,
                id -> "goes-glm".equals(id) ? HealthStatus.DISABLED : HealthStatus.HEALTHY, flashes,
                new MutableClock(NOW));
    }

    static BriefingSectionContributor.Context context(List<String> places) {
        DigestStats stats = new DigestStats(100, 200, FROM, NOW, false, null, null, null, null, null, null, true, null);
        return new BriefingSectionContributor.Context(stats, List.of(), List.of(), null, FROM, NOW,
                java.time.ZoneOffset.UTC, null, Set.of(), places, List.of());
    }

    static final String GOLDEN = """
            ## Weather at places

            ### Home (America/Chicago)

            | Section | Weather |
            |---|---|
            | Now: radar | max 52.5 dBZ in the radius; nearest 50 dBZ 14 km |
            | Now: lightning | Lightning: data stale since 06:30 |
            | Now: NWS alerts | Severe Thunderstorm Warning (Severe, until 2026-10-12 08:45 CDT) |
            | Period | max 55 dBZ; MESH 19 mm; lightning total unavailable (partial period) |
            | Hazards that entered | probsevere-object ps-1 (probsevere-object, 05:15) |
            | Next: NWS | PoP 70 %, QPF 12.5 mm, gusts 14.2 m/s, 18 to 31 °C (24 h) |
            | Next: GFS | gusts up to 21 m/s at 2026-10-12 18:00 CDT, 30 mm precipitation (run 2026-10-12T06:00:00Z) |
            | Next: HRRR | HRRR: unavailable (not evidence of clear weather) |

            ### Tokyo (Asia/Tokyo)

            | Section | Weather |
            |---|---|
            | Now: radar | Radar: no coverage (not evidence of clear weather) |
            | Now: lightning | 0 GLM flashes within 15 km (15 min); nearest none within 100 km |
            | Now: NWS alerts | none active |
            | Period | max unknown; MESH unknown; lightning total unavailable (partial period) |
            | Next: NWS | NWS forecast: unavailable (not evidence of clear weather) |
            | Next: GFS | gusts up to 21 m/s at 2026-10-13 08:00 JST, 30 mm precipitation (run 2026-10-12T06:00:00Z) |
            | Next: HRRR | HRRR: unavailable (not evidence of clear weather) |

            _Automated — not an official warning. Stale or unavailable data is unknown, not calm._
            """;

    @Test
    @DisplayName("now, period and next for two places in their timezones; stale labeled; partial period; golden markdown")
    @SuppressWarnings("unchecked")
    void sectionGolden() {
        BriefingSection section = section().contribute(context(List.of("wx-home", "wx-tokyo"))).orElseThrow();

        assertThat(section.key()).isEqualTo(WeatherAtPlacesSection.KEY);
        assertThat(section.markdown()).isEqualTo(GOLDEN);
        List<Map<String, Object>> digestPlaces = (List<Map<String, Object>>) section.data().get("places");
        assertThat(digestPlaces).hasSize(2);
        Map<String, Object> home = digestPlaces.get(0);
        assertThat(home).containsEntry("placeId", "wx-home").containsEntry("timezone", "America/Chicago");
        Map<String, Object> now = (Map<String, Object>) home.get("now");
        assertThat((Map<String, Object>) now.get("lightning")).containsEntry("status", "stale")
                .containsEntry("asOf", "2026-10-12T11:30:00Z").doesNotContainKey("count15km15m");
        Map<String, Object> period = (Map<String, Object>) home.get("period");
        assertThat(period).containsEntry("partial", true).containsEntry("maxDbz", 55.0).containsEntry("maxMeshMm", 19.0)
                .containsEntry("lightningTotal", null);
        assertThat((List<String>) period.get("notes")).anyMatch(n -> n.startsWith("lightning total unavailable"));
        Map<String, Object> gfs = (Map<String, Object>) ((Map<String, Object>) home.get("next")).get("gfs");
        assertThat(gfs).containsEntry("status", "ok").containsEntry("run", "2026-10-12T06:00:00Z")
                .containsEntry("gustMaxMs", 21.0).containsEntry("precipMm", 30.0)
                .containsEntry("validTime", "2026-10-12T23:00:00Z");
        Map<String, Object> llm = (Map<String, Object>) section.llmContext();
        assertThat(llm).containsEntry("instruction", WeatherAtPlacesSection.LLM_INSTRUCTION);
    }

    @Test
    @DisplayName("no places: no section")
    void noPlaces() {
        assertThat(section().contribute(context(List.of()))).isEmpty();
    }

    // ------------------------------------------------------------------ wiring on the briefing context

    void createPlaces() {
        placeService.create(json("{\"id\":\"wx-home\",\"name\":\"Home\",\"shape\":\"point\",\"geometry\":{\"type\":"
                + "\"Point\",\"coordinates\":[-97.75,30.27]},\"radiusKm\":20,\"timezone\":\"America/Chicago\"}"));
        placeService.create(json("{\"id\":\"wx-tokyo\",\"name\":\"Tokyo\",\"shape\":\"point\",\"geometry\":{\"type\":"
                + "\"Point\",\"coordinates\":[139.7,35.6]},\"radiusKm\":20,\"timezone\":\"Asia/Tokyo\"}"));
    }

    static String weatherBriefing(String mode) {
        return "{\"name\":\"Weather brief\",\"schedule\":\"0 7 * * *\",\"timezone\":\"UTC\",\"scope\":{\"kinds\":[\""
                + BriefingTestSources.QUAKE_KIND + "\"],\"places\":[\"wx-home\",\"wx-tokyo\"]},\"lookback\":\"24h\","
                + "\"mode\":\"" + mode + "\"}";
    }

    @Test
    @DisplayName("a digest briefing renders the section with every part labeled; the partial-period note shows")
    void digestBriefing() {
        createPlaces();
        retentionState.setLowestRetainedSeq(Long.MAX_VALUE);

        Map<String, Object> run = briefings.runNow(id(briefings.create(json(weatherBriefing("digest")), admin())));

        String markdown = run.get("contentMarkdown").toString();
        assertThat(markdown).contains("## Weather at places").contains("### Home (America/Chicago)")
                .contains("### Tokyo (Asia/Tokyo)").contains("(partial period)")
                .contains("Radar: unavailable (not evidence of clear weather)")
                .contains("not an official warning");
    }

    @Test
    @DisplayName("an LLM briefing receives the section as a data block with the unknown-is-not-clear instruction")
    void llmBriefing() {
        createPlaces();
        useChatProvider();
        chat().then(request -> text("Weather near Home is unknown: radar data is unavailable."));

        Map<String, Object> run = briefings.runNow(id(briefings.create(json(weatherBriefing("llm")), admin())));

        assertThat(run.get("status")).isEqualTo("succeeded");
        ChatRequest request = chat().requests().getLast();
        assertThat(request.system()).isEqualTo(BriefingPrompts.SYSTEM)
                .contains("Weather sections with status other than ok are unknown, not clear");
        String user = ((ChatMessage.User) request.messages().getLast()).text();
        int dataStart = user.indexOf("<data>");
        assertThat(user.indexOf("\"" + WeatherAtPlacesSection.KEY + "\"")).isGreaterThan(dataStart);
        assertThat(user).contains(WeatherAtPlacesSection.LLM_INSTRUCTION).contains("\"wx-home\"");
    }

    @Test
    @DisplayName("/v1/digest with places includes weatherAtPlaces; 401 without a token; 400 for an unknown place")
    @SuppressWarnings("unchecked")
    void digestEndpoint() {
        createPlaces();
        DigestService.Params params = new DigestService.Params(null, null, List.of(), List.of(), null, 5, true,
                List.of("wx-home"));

        Map<String, Object> digest = digests.digest(params, admin());

        List<Map<String, Object>> places = (List<Map<String, Object>>) digest.get("weatherAtPlaces");
        assertThat(places).singleElement().satisfies(p -> assertThat(p).containsEntry("placeId", "wx-home"));
        assertThat((Map<String, Object>) digest.get("sections")).containsKey(WeatherAtPlacesSection.KEY);

        McpCaller anonymous = new McpCaller(null, new ReadCaller(Scope.READ, false, true), Set.of(), "127.0.0.1");
        assertThatThrownBy(() -> digests.digest(params, anonymous)).isInstanceOf(ForesignalException.class)
                .hasMessageContaining("token");
        DigestService.Params unknown = new DigestService.Params(null, null, List.of(), List.of(), null, 5, true,
                List.of("nowhere"));
        assertThatThrownBy(() -> digests.digest(unknown, admin())).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("unknown scope.places and scope.weatherProducts ids are refused")
    void validation() {
        String body = "{\"name\":\"Bad\",\"schedule\":\"0 7 * * *\",\"timezone\":\"UTC\",\"scope\":{\"places\":[\"nowhere\"],"
                + "\"weatherProducts\":[\"no-such-product\"]},\"lookback\":\"24h\",\"mode\":\"digest\"}";
        assertThatThrownBy(() -> briefings.create(json(body), admin())).isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().toString()).contains("scope.places")
                        .contains("scope.weatherProducts"));
    }
}

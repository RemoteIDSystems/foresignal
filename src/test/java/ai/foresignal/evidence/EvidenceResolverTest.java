package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.stream.ChangeBus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The evidence resolver's rules (prompt 116): tolerance rules per family, the upstream-id match ahead of
 * proximity, evidence grouping by originator, independence group, aggregation and citation, the quality flags,
 * and merging on a double match within one family only.
 */
class EvidenceResolverTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    private static CanonicalRules rules() {
        return new CanonicalRules(JSON);
    }

    private static ObservationFacts quake(String source, String id, double lon, double lat, Instant at, Double mag,
            List<String> upstream) {
        return new ObservationFacts(source, id, 1, 1, at, at, lon, lat, mag, null, null, null, null, null, upstream, null);
    }

    // ------------------------------------------------------------------------- tolerances

    @Test
    @DisplayName("earthquakes: Δt ≤ 60 s, ≤ 100 km and |ΔM| ≤ 0.8 match; beyond any of them they do not")
    void earthquakeTolerances() {
        CanonicalRules.FamilyRule seismic = rules().family("seismic");
        ObservationFacts usgs = quake("a", "1", 30.0, 38.0, T0, 5.0, List.of());
        Optional<EvidenceMatcher.Match> near = EvidenceMatcher.match(seismic, usgs,
                quake("b", "2", 30.1, 38.05, T0.plusSeconds(10), 5.1, List.of()));
        assertThat(near).isPresent();
        assertThat(near.get().confidence()).isBetween(0.5, 1.0);
        assertThat(near.get().reason()).containsEntry("rule", "proximity").containsEntry("upstreamIdMatch", false);
        assertThat(EvidenceMatcher.match(seismic, usgs, quake("b", "2", 30.0, 38.0, T0.plusSeconds(61), 5.0, List.of())))
                .isEmpty();
        assertThat(EvidenceMatcher.match(seismic, usgs, quake("b", "2", 32.0, 38.0, T0, 5.0, List.of()))).isEmpty();
        assertThat(EvidenceMatcher.match(seismic, usgs, quake("b", "2", 30.0, 38.0, T0, 5.9, List.of()))).isEmpty();
    }

    @Test
    @DisplayName("the confidence is 1 − the largest normalized delta, floored at 0.3")
    void confidenceFormula() {
        assertThat(EvidenceMatcher.confidence(0.0)).isEqualTo(1.0);
        assertThat(EvidenceMatcher.confidence(0.25)).isEqualTo(0.75);
        assertThat(EvidenceMatcher.confidence(0.95)).isEqualTo(EvidenceMatcher.FLOOR);
    }

    @Test
    @DisplayName("fires: ≤ 1 km and ≤ 1 h")
    void fireTolerances() {
        CanonicalRules.FamilyRule fire = rules().family("fire");
        ObservationFacts a = quake("firms", "1", 10.0, 45.0, T0, null, List.of());
        assertThat(EvidenceMatcher.match(fire, a, quake("goes", "2", 10.002, 45.002, T0.plusSeconds(600), null,
                List.of()))).isPresent();
        assertThat(EvidenceMatcher.match(fire, a, quake("goes", "2", 10.05, 45.0, T0, null, List.of()))).isEmpty();
        assertThat(EvidenceMatcher.match(fire, a, quake("goes", "2", 10.0, 45.0, T0.plusSeconds(3_700), null,
                List.of()))).isEmpty();
    }

    @Test
    @DisplayName("conflict events: same 0.1° cell and UTC day only")
    void conflictCellDay() {
        CanonicalRules.FamilyRule conflict = rules().family("conflict");
        ObservationFacts a = quake("acled", "1", 44.21, 15.35, T0, null, List.of());
        assertThat(EvidenceMatcher.match(conflict, a, quake("ucdp", "2", 44.29, 15.31, T0.plusSeconds(3_600), null,
                List.of()))).isPresent();
        assertThat(EvidenceMatcher.match(conflict, a, quake("ucdp", "2", 44.31, 15.35, T0, null, List.of()))).isEmpty();
        assertThat(EvidenceMatcher.match(conflict, a, quake("ucdp", "2", 44.21, 15.35, T0.plusSeconds(86_400), null,
                List.of()))).isEmpty();
    }

    @Test
    @DisplayName("outages: same country or ASN with overlapping intervals")
    void outages() {
        CanonicalRules.FamilyRule outage = rules().family("outage");
        ObservationFacts a = new ObservationFacts("ioda", "1", 1, 1, T0, T0, null, null, null, "IR", null, T0,
                T0.plusSeconds(7_200), null, List.of(), null);
        ObservationFacts b = new ObservationFacts("cf", "2", 1, 1, T0, T0, null, null, null, "IR", null,
                T0.plusSeconds(3_600), T0.plusSeconds(9_000), null, List.of(), null);
        ObservationFacts other = new ObservationFacts("cf", "3", 1, 1, T0, T0, null, null, null, "IQ", null, T0,
                T0.plusSeconds(3_600), null, List.of(), null);
        ObservationFacts later = new ObservationFacts("cf", "4", 1, 1, T0, T0, null, null, null, "IR", null,
                T0.plusSeconds(30_000), T0.plusSeconds(40_000), null, List.of(), null);
        assertThat(EvidenceMatcher.match(outage, a, b)).map(EvidenceMatcher.Match::confidence).contains(0.8);
        assertThat(EvidenceMatcher.match(outage, a, other)).isEmpty();
        assertThat(EvidenceMatcher.match(outage, a, later)).isEmpty();
    }

    @Test
    @DisplayName("an upstream-id match beats proximity: same id far apart is one event with confidence 1")
    void upstreamIdBeatsProximity() {
        CanonicalRules.FamilyRule seismic = rules().family("seismic");
        ObservationFacts a = quake("gdacs", "1", 30.0, 38.0, T0, 5.0, List.of("glide:EQ-2026-000001-TUR"));
        ObservationFacts far = quake("reliefweb", "2", 35.0, 39.0, T0.plusSeconds(7_200), null,
                List.of("glide:EQ-2026-000001-TUR"));
        Optional<EvidenceMatcher.Match> match = EvidenceMatcher.match(seismic, a, far);
        assertThat(match).isPresent();
        assertThat(match.get().confidence()).isEqualTo(1.0);
        assertThat(match.get().reason()).containsEntry("rule", EvidenceMatcher.UPSTREAM_RULE)
                .containsEntry("upstreamIdMatch", true);
    }

    @Test
    @DisplayName("weather alerts link only by CAP identifier, and disasters by hazard type within 200 km and 24 h")
    void identifierAndHazard() {
        CanonicalRules r = rules();
        ObservationFacts nws = new ObservationFacts("nws", "urn:1", 1, 1, T0, T0, -90.0, 30.0, null, null, null, null,
                null, null, List.of("cap-identifier:urn:1"), null);
        ObservationFacts cap = new ObservationFacts("cap-x", "c", 1, 1, T0, T0, -90.0, 30.0, null, null, null, null,
                null, null, List.of("cap-identifier:urn:1"), null);
        ObservationFacts unrelated = new ObservationFacts("cap-x", "d", 1, 1, T0, T0, -90.0, 30.0, null, null, null,
                null, null, null, List.of("cap-identifier:urn:2"), null);
        assertThat(EvidenceMatcher.match(r.family("weather-alert"), nws, cap)).isPresent();
        assertThat(EvidenceMatcher.match(r.family("weather-alert"), nws, unrelated)).isEmpty();
        ObservationFacts flood = new ObservationFacts("gdacs", "f", 1, 1, T0, T0, 90.0, 23.0, null, null, null, null,
                null, r.hazard("FL"), List.of(), null);
        ObservationFacts flood2 = new ObservationFacts("reliefweb", "g", 1, 1, T0.plusSeconds(36_000), T0, 90.5, 23.2,
                null, null, null, null, null, r.hazard("Flood"), List.of(), null);
        ObservationFacts quake = new ObservationFacts("reliefweb", "h", 1, 1, T0, T0, 90.0, 23.0, null, null, null,
                null, null, r.hazard("Earthquake"), List.of(), null);
        assertThat(EvidenceMatcher.match(r.family("disaster"), flood, flood2)).isPresent();
        assertThat(EvidenceMatcher.match(r.family("disaster"), flood, quake)).isEmpty();
    }

    @Test
    @DisplayName("GDACS earthquakes belong to the seismic family; other GDACS disasters to disaster")
    void familyOf() {
        CanonicalRules r = rules();
        assertThat(r.familyOf("disaster", Map.of("eventType", "EQ")).family()).isEqualTo("seismic");
        assertThat(r.familyOf("disaster", Map.of("eventType", "FL")).family()).isEqualTo("disaster");
        assertThat(r.familyOf("earthquake", Map.of()).family()).isEqualTo("seismic");
        assertThat(r.familyOf("aircraft", Map.of())).isNull();
        assertThat(r.family("seismic").bands().bandOf(4.7)).isEqualTo("M4.5-5");
        assertThat(r.family("seismic").bands().bandOf(7.3)).isEqualTo("M7+");
        assertThat(CanonicalRules.Bands.lowerBound("M4.5-5")).isEqualTo(4.5);
    }

    @Test
    @DisplayName("facts read the magnitude, upstream ids ($externalId and namespaces) and the cited domain")
    void facts() {
        CanonicalRules r = rules();
        ObservationFacts f = FactsExtractor.extract(r.family("conflict"), UpstreamRefs.parse(List.of("cite:sourceUrl",
                "$externalId@gdelt")), r, "gdelt-events", "e1", 5, 1, T0, T0, 44.0, 15.0,
                Map.of("sourceUrl", "https://www.Example-News.org/2026/story"));
        assertThat(f.citation()).isEqualTo("example-news.org");
        assertThat(f.upstreamIds()).containsExactly("gdelt:e1");
        ObservationFacts q = FactsExtractor.extract(r.family("seismic"), UpstreamRefs.NONE, r, "usgs", "u1", 1, 1, T0,
                T0, 30.0, 38.0, Map.of("mag", 5.2));
        assertThat(q.attribute()).isEqualTo(5.2);
    }

    // ------------------------------------------------------------------------- grouping

    private static EvidenceGrouper.Member member(String key, String source, String originator, String citation,
            OriginatorRegistry registry) {
        return new EvidenceGrouper.Member(key, source, registry.resolve(source), citation, Long.parseLong(key));
    }

    @Test
    @DisplayName("grouping: one originator, one independence group, aggregation and citation collapse; others stay apart")
    void grouping() throws Exception {
        OriginatorRegistry registry = OriginatorRegistry.of(OriginatorRegistryTest.bundled(), JSON);
        List<EvidenceGrouper.Member> members = new ArrayList<>();
        members.add(member("1", "usgs-earthquakes", null, null, registry));
        members.add(member("2", "usgs-shakemap", null, null, registry)); // same originator
        members.add(member("3", "gdacs", null, null, registry)); // aggregates USGS
        members.add(member("4", "emsc-seismic", null, null, registry)); // independent
        members.add(member("5", "gdelt-events", null, "usgs.gov", registry)); // cites USGS
        members.add(member("6", "gdelt-events", null, "example-news.org", registry)); // cites an outlet
        members.add(member("7", "gdelt-events", null, "example-news.org", registry)); // same outlet
        members.add(member("8", "adsblol-mil", null, null, registry));
        members.add(member("9", "opensky-states", null, null, registry)); // same independence group
        List<EvidenceGrouper.Group> groups = EvidenceGrouper.group(members, registry);
        assertThat(groups).hasSize(4);
        assertThat(groups.get(0).members()).containsExactly("1", "2", "3", "5");
        assertThat(groups.get(0).root()).isEqualTo("usgs");
        assertThat(groups.get(1).members()).containsExactly("4");
        assertThat(groups.get(2).members()).containsExactly("6", "7");
        assertThat(groups.get(2).root()).isEqualTo("cite:example-news.org");
        assertThat(groups.get(3).members()).containsExactly("8", "9");
    }

    // ------------------------------------------------------------------------- flags

    @Test
    @DisplayName("ECHO: five observations of one group within 2 h; SINGLE_ORIGINATOR: one group")
    void echoFlag() {
        CanonicalRules.FamilyRule conflict = rules().family("conflict");
        List<QualityFlags.Member> members = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            members.add(new QualityFlags.Member("cite:x", T0, T0.plusSeconds(i * 600L), false));
        }
        assertThat(QualityFlags.compute(conflict, members)).containsExactly(QualityFlags.ECHO,
                QualityFlags.SINGLE_ORIGINATOR);
        List<QualityFlags.Member> spread = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            spread.add(new QualityFlags.Member("cite:x", T0, T0.plusSeconds(i * 3_600L), false));
        }
        assertThat(QualityFlags.compute(conflict, spread)).doesNotContain(QualityFlags.ECHO);
    }

    @Test
    @DisplayName("TIMESTAMP_CONFLICT beyond 3 × the family tolerance, and LATE_REVISION from a revised member")
    void conflictAndRevision() {
        CanonicalRules.FamilyRule seismic = rules().family("seismic");
        List<QualityFlags.Member> members = List.of(new QualityFlags.Member("usgs", T0, T0, false),
                new QualityFlags.Member("emsc", T0.plusSeconds(181), T0, true));
        assertThat(QualityFlags.compute(seismic, members)).containsExactly(QualityFlags.TIMESTAMP_CONFLICT,
                QualityFlags.LATE_REVISION);
        List<QualityFlags.Member> close = List.of(new QualityFlags.Member("usgs", T0, T0, false),
                new QualityFlags.Member("emsc", T0.plusSeconds(170), T0, false));
        assertThat(QualityFlags.compute(seismic, close)).isEmpty();
        assertThat(EvidenceResolver.revised(quake("a", "1", 30, 38, T0, 5.0, List.of()),
                quake("a", "1", 30, 38, T0.plusSeconds(5), 5.0, List.of()))).isTrue();
        assertThat(EvidenceResolver.revised(quake("a", "1", 30, 38, T0, 5.0, List.of()),
                quake("a", "1", 30.1, 38, T0, 5.0, List.of()))).isTrue();
        assertThat(EvidenceResolver.revised(quake("a", "1", 30, 38, T0, 5.0, List.of()),
                quake("a", "1", 30, 38, T0, 5.3, List.of()))).isFalse();
    }

    // ------------------------------------------------------------------------- merging

    @SuppressWarnings("unchecked")
    private EvidenceResolver resolver(EvidenceStore store) throws Exception {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(any(), any())).thenReturn(Optional.empty());
        ObjectProvider<ai.foresignal.reference.RegionTagger> regions = mock(ObjectProvider.class);
        TransactionTemplate tx = mock(TransactionTemplate.class);
        return new EvidenceResolver(mock(ChangeBus.class), mock(JdbcTemplate.class), JSON, settings, rules(),
                OriginatorRegistry.of(OriginatorRegistryTest.bundled(), JSON), store, new EvidenceEventBus(), regions,
                tx, Clock.fixed(T0, ZoneOffset.UTC), false);
    }

    private static EvidenceStore.StoredLink stored(String event, String source, String id, double lon) {
        return new EvidenceStore.StoredLink("L" + id, event, quake(source, id, lon, 38.0, T0, 5.0, List.of()), "usgs-anss",
                "usgs", null, "G" + event, 1.0, Map.of(), false, T0);
    }

    private static EvidenceStore.StoredEvent event(String id, Instant created) {
        return new EvidenceStore.StoredEvent(id, "seismic", T0, 30.0, 38.0, List.of(), 5.0, T0, "record:x/y@1", 1, 1,
                List.of(), List.of("usgs-earthquakes"), "open", null, "1.0.0", created, created);
    }

    @Test
    @DisplayName("an observation matching two events of its family merges them into the older one, then links")
    void mergeOnDoubleMatch() throws Exception {
        EvidenceStore store = mock(EvidenceStore.class);
        when(store.linksOf("emsc-seismic", "e1")).thenReturn(List.of());
        when(store.byUpstream(eq("seismic"), anyList())).thenReturn(List.of());
        when(store.candidates(eq("seismic"), any(), any(), any())).thenReturn(List.of("EVT-NEW", "EVT-OLD"));
        when(store.links("EVT-OLD")).thenReturn(List.of(stored("EVT-OLD", "usgs-earthquakes", "u1", 30.0)));
        when(store.links("EVT-NEW")).thenReturn(List.of(stored("EVT-NEW", "geofon", "g1", 30.4)));
        when(store.events(anyList())).thenReturn(List.of(event("EVT-NEW", T0.plusSeconds(10)), event("EVT-OLD", T0)));
        when(store.event(any())).thenReturn(Optional.of(event("EVT-OLD", T0)));
        when(store.groups(any())).thenReturn(List.of());
        EvidenceResolver resolver = resolver(store);
        resolver.process(new EvidenceResolver.VersionRow(10, "emsc-seismic", "e1", "earthquake", 1, T0.plusSeconds(5),
                T0.plusSeconds(5), 30.2, 38.0, Map.of("mag", 5.1)));
        verify(store).merge(eq("EVT-NEW"), eq("EVT-OLD"), any());
        verify(store).insertLink(eq("EVT-OLD"), any(), any(), anyDouble(), anyMap(), any());
    }

    @Test
    @DisplayName("candidates are only ever read within the observation's own family: never merged across families")
    void neverAcrossFamilies() throws Exception {
        EvidenceStore store = mock(EvidenceStore.class);
        when(store.linksOf(any(), any())).thenReturn(List.of());
        when(store.byUpstream(any(), anyList())).thenReturn(List.of());
        when(store.candidates(any(), any(), any(), any())).thenReturn(List.of());
        when(store.insertEvent(any(), any(), anyList(), any())).thenReturn("EVT-1");
        when(store.links("EVT-1")).thenReturn(List.of());
        when(store.event("EVT-1")).thenReturn(Optional.empty());
        EvidenceResolver resolver = resolver(store);
        resolver.process(new EvidenceResolver.VersionRow(11, "nasa-firms", "f1", "fire-detection", 1, T0, T0, 10.0,
                45.0, Map.of()));
        verify(store).candidates(eq("fire"), any(), any(), any());
        verify(store, never()).candidates(eq("seismic"), any(), any(), any());
        verify(store, never()).merge(any(), any(), any());
        verify(store).insertEvent(eq("fire"), any(), anyList(), any());
    }

    @Test
    @DisplayName("a version at or below the stored seq of its record is skipped (exactly once)")
    void exactlyOnce() throws Exception {
        EvidenceStore store = mock(EvidenceStore.class);
        EvidenceStore.StoredLink existing = new EvidenceStore.StoredLink("L1", "EVT", new ObservationFacts(
                "usgs-earthquakes", "u1", 20, 2, T0, T0, 30.0, 38.0, 5.0, null, null, null, null, null, List.of(), null),
                "usgs-anss", "usgs", null, "G", 1.0, Map.of(), false, T0);
        when(store.linksOf("usgs-earthquakes", "u1")).thenReturn(List.of(existing));
        EvidenceResolver resolver = resolver(store);
        resolver.process(new EvidenceResolver.VersionRow(20, "usgs-earthquakes", "u1", "earthquake", 2, T0, T0, 30.0,
                38.0, Map.of("mag", 5.0)));
        verify(store, never()).updateLink(any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean(), any());
        verify(store, never()).insertLink(any(), any(), any(), anyDouble(), anyMap(), any());
    }
}

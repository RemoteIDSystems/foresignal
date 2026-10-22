package ai.foresignal.testsupport.social;

import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.ObservationFacts;
import ai.foresignal.evidence.Originator;
import ai.foresignal.social.correlation.CompareRules;
import ai.foresignal.social.correlation.PhysicalEvidenceSource;
import ai.foresignal.social.correlation.SocialPhysicalComparator;
import ai.foresignal.social.narrative.NarrativeStore;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixtures of the prompt 137 tests: narratives and claims written straight into the 136 tables, canonical events
 * through the 116 store, family rules, detectability and evidence values.
 */
public final class CorrelationTestSupport {

    /** Aden, Yemen. */
    public static final double LON = 45.03;

    /** Aden, Yemen. */
    public static final double LAT = 12.78;

    private CorrelationTestSupport() {}

    /** A conflict rule: 6 h before, 24 h after, 25 km, 24 h publication lag. */
    public static CompareRules.FamilyRule conflictRule() {
        return new CompareRules.FamilyRule("conflict", Duration.ofHours(6), Duration.ofHours(24), 25,
                Duration.ofHours(24), false, List.of("conflict"), List.of("conflict"), List.of());
    }

    /** Detectability that can read an absence. */
    public static SocialPhysicalComparator.Detectability covered() {
        return new SocialPhysicalComparator.Detectability(0.92, 0.85, 0.97, List.of("declared"), true, null);
    }

    /** Detectability that cannot. */
    public static SocialPhysicalComparator.Detectability thin() {
        return new SocialPhysicalComparator.Detectability(0.3, 0.2, 0.4, List.of("declared"), false,
                "LOW_DETECTABILITY");
    }

    /** Non-social physical evidence. */
    public static PhysicalEvidenceSource.Evidence physical(String ref, String originatorClass, Instant at) {
        return new PhysicalEvidenceSource.Evidence(ref, "conflict", "acled", originatorClass, at, 3.0, false);
    }

    /** Social evidence (must never count). */
    public static PhysicalEvidenceSource.Evidence social(String ref, Instant at) {
        return new PhysicalEvidenceSource.Evidence(ref, "social-narrative", "social:bluesky:abc", "social", at, 0.0,
                false);
    }

    /** A negating record. */
    public static PhysicalEvidenceSource.Evidence negating(String ref, Instant at) {
        return new PhysicalEvidenceSource.Evidence(ref, "outage", "statuspage", "agency", at, 5.0, true);
    }

    /** A locality {@code ClaimPlace}. */
    public static Map<String, Object> place(String name, double lon, double lat, String region) {
        Map<String, Object> place = new LinkedHashMap<>();
        place.put("ref", null);
        place.put("name", name);
        place.put("geom", Map.of("type", "Point", "coordinates", List.of(lon, lat)));
        place.put("precision", "locality");
        place.put("confidence", 0.9);
        place.put("regionCode", region);
        return place;
    }

    /**
     * Writes a narrative.
     *
     * @param store the narrative store
     * @param id the narrative id
     * @param at its first post
     * @param groups its evidence groups
     * @param situations its situations
     * @param velocity its velocity, or null
     */
    public static void narrative(NarrativeStore store, String id, Instant at, int groups, List<String> situations,
            Double velocity) {
        store.insertNarrative(id, "Explosion reported at the port of Aden", null, null, List.of("red-sea"),
                List.of(), List.of(), List.of("YE"), List.of("en"), at, "CLEAR", null, at);
        store.jdbc().update("UPDATE social_narrative SET evidence_group_count = ?, post_count = ?, unique_authors = ?,"
                + " situation_ids = ?, velocity = ?, platforms = CAST(? AS jsonb), dirty = false, last_activity_at = ?"
                + " WHERE id = ?", groups, groups * 3, groups * 2, situations.toArray(String[]::new), velocity,
                "[{\"platform\":\"bluesky\",\"posts\":3}]", Timestamp.from(at.plusSeconds(600)), id);
    }

    /**
     * Writes an asserted claim at Aden.
     *
     * @return the claim id
     */
    public static String claim(NarrativeStore store, String id, String narrativeId, String type, String family,
            Instant at, int groups) {
        NarrativeStore.Claim c = new NarrativeStore.Claim(id, narrativeId, type + "|asserted|aden", type, family,
                "asserted", false, place("Aden", LON, LAT, "YE"), LON, LAT, "YE", List.of(), null, null, groups * 2,
                groups, at, "deterministic", null, 0.8, "unassessed", null, "account:bluesky:k", "F", 6, "CLEAR", at,
                at, null, null, null, null);
        return store.upsertClaim(c).getKey();
    }

    /**
     * Writes a canonical event with one link.
     *
     * @return the canonical event id
     */
    public static String canonicalEvent(EvidenceStore store, org.springframework.jdbc.core.JdbcTemplate jdbc,
            String family, String sourceId, String originatorId, String originatorClass, Instant at, double lon,
            double lat, String region) {
        ObservationFacts facts = new ObservationFacts(sourceId, "e-" + at.toEpochMilli(), 1, 1, at, at, lon, lat, null,
                region, null, null, null, null, List.of(), null);
        String id = store.insertEvent(family, facts, List.of(region), at);
        store.insertLink(id, facts, new Originator(originatorId, originatorClass, null, List.of(), null, List.of()), 1.0,
                Map.of("reason", "seed"), at);
        jdbc.update("UPDATE canonical_event SET observation_count = 1, evidence_group_count = 1 WHERE id = ?",
                id);
        return id;
    }
}

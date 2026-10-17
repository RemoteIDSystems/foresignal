package ai.foresignal.situation;

import ai.foresignal.evidence.RecordRef;
import ai.foresignal.intel.SignalFamilies;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.search.DocInput;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.trigger.window.WindowEngine;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the situation linker's inputs (prompt 118 §2) and resolves manual members, normalizing each into a
 * {@link LinkerInput}:
 *
 * <ul>
 *   <li>{@code intelligence-event} records (101) whose status is {@code emerging} or {@code active};
 *   <li>{@code analytic-finding} records (119 onward) of severity notable or higher;
 *   <li>alerts (017) whose rule carries the tag {@code situation} or whose severity is critical (exercise alerts
 *       and alerts on situation records never);
 *   <li>{@code rule-windows} {@code anomaly} records (100) classified {@code activity-up} or {@code activity-down}.
 * </ul>
 *
 * <p>Only the stored observations are read; nothing is written (MEM-1). Regions come from the 040
 * {@link RegionTagger}, entities from open 098 {@code entity_link} rows of the records an input rests on, themes
 * from the {@link ThemeTable}. An intelligence event active with severity warning or higher, a significant finding
 * or a critical alert may seed a situation.
 */
@Component
public class LinkerInputReader {

    /** The intelligence-event record kind (101). */
    public static final String INTEL_KIND = "intelligence-event";

    /** The analytic-finding record kind (119). */
    public static final String FINDING_KIND = "analytic-finding";

    /** The alert tag that sends an alert to the linker whatever its severity. */
    public static final String SITUATION_TAG = "situation";

    /** The anomaly classifications the linker reads. */
    static final Set<String> ANOMALY_CLASSES = Set.of("activity-up", "activity-down");

    /** The finding severities the linker reads (notable or higher). */
    static final Set<String> FINDING_SEVERITIES = Set.of("notable", "significant", "critical");

    /** The intelligence-event statuses the linker reads. */
    static final Set<String> INTEL_STATUSES = Set.of("emerging", "active");

    /** Kinds whose {@link SignalFamilies#forKind} family really is {@code conflict}. */
    static final Set<String> CONFLICT_KINDS = Set.of("conflict-event", "air-raid-alert", "frontline");

    /** Payload fields that hold text worth matching topic keywords against. */
    static final List<String> TEXT_FIELDS = List.of("title", "name", "headline", "description", "summary",
            "message", "text", "place");

    private static final Pattern REGION_CODE = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};
    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {};

    private static final String VERSION_COLUMNS = "seq, source_id, external_id, kind, version, observed_at,"
            + " ST_AsText(geom) AS wkt, payload::text AS payload";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final ThemeTable themes;
    private final ObjectProvider<RegionTagger> regions;
    private final ObjectProvider<EgressPolicy> egress;

    /** Creates the reader. */
    public LinkerInputReader(JdbcTemplate jdbc, JsonMapper json, ThemeTable themes, ObjectProvider<RegionTagger> regions,
            ObjectProvider<EgressPolicy> egress) {
        this.jdbc = jdbc;
        this.json = json;
        this.themes = themes;
        this.regions = regions;
        this.egress = egress;
    }

    /** A version row. */
    record VersionRow(long seq, String sourceId, String externalId, String kind, int version, Instant observedAt,
            Geometry geom, Map<String, Object> payload) {}

    // ------------------------------------------------------------------------- linker inputs

    /** The linker inputs whose seq lies in {@code (from, to]}, in seq order. */
    public List<LinkerInput> read(long from, long to) {
        List<LinkerInput> out = new ArrayList<>();
        for (VersionRow row : versions("WHERE seq > ? AND seq <= ? AND tombstone = false AND persist_reason <> 8"
                + " AND (kind IN (?, ?) OR (source_id = ? AND kind = ?)) ORDER BY seq", from, to, INTEL_KIND,
                FINDING_KIND, WindowEngine.SOURCE_ID, WindowEngine.ANOMALY_KIND)) {
            LinkerInput input = fromVersion(row, true);
            if (input != null) {
                out.add(input);
            }
        }
        // Alerts on situation records themselves are never inputs: linking them would feed a situation back into
        // itself.
        out.addAll(alerts("WHERE NOT a.exercise AND a.seq > ? AND a.seq <= ? AND (a.severity = 'critical'"
                + " OR a.tags @> CAST(? AS jsonb)) AND (a.source_id IS DISTINCT FROM ? OR a.kind IS DISTINCT FROM ?)"
                + " ORDER BY a.seq, a.id",
                true, from, to, "[\"" + SITUATION_TAG + "\"]", SituationRecordPublisher.SOURCE_ID,
                SituationRecordPublisher.KIND));
        out.sort(Comparator.comparingLong(LinkerInput::seq).thenComparing(LinkerInput::memberType)
                .thenComparing(LinkerInput::ref));
        return out;
    }

    // ------------------------------------------------------------------------- manual members

    /**
     * Resolves a member an operator or agent names, without the linker's eligibility filters.
     *
     * @param memberType the member type
     * @param ref the ref (an id, or {@code sourceId/externalId} for records and anomalies)
     * @param seq the pinned seq; required for records and anomalies, verified against the ref when given
     * @return the member, or empty when it does not exist or the seq does not match it
     */
    public Optional<LinkerInput> resolve(String memberType, String ref, Long seq) {
        if (ref == null || ref.isBlank()) {
            return Optional.empty();
        }
        switch (memberType) {
            case SituationMemberTypes.RECORD, SituationMemberTypes.ANOMALY -> {
                int slash = ref.indexOf('/');
                if (seq == null || slash <= 0) {
                    return Optional.empty();
                }
                String source = ref.substring(0, slash);
                String external = ref.substring(slash + 1);
                List<VersionRow> rows = versions("WHERE seq = ? AND source_id = ? AND external_id = ?", seq, source,
                        external);
                if (rows.isEmpty()) {
                    return Optional.empty();
                }
                VersionRow row = rows.get(0);
                if (SituationMemberTypes.ANOMALY.equals(memberType)
                        && !WindowEngine.ANOMALY_KIND.equals(row.kind())) {
                    return Optional.empty();
                }
                return Optional.ofNullable(SituationMemberTypes.ANOMALY.equals(memberType) ? anomaly(row, false)
                        : record(row));
            }
            case SituationMemberTypes.INTEL_EVENT, SituationMemberTypes.FINDING -> {
                String kind = SituationMemberTypes.INTEL_EVENT.equals(memberType) ? INTEL_KIND : FINDING_KIND;
                List<VersionRow> rows = seq != null
                        ? versions("WHERE seq = ? AND external_id = ? AND kind = ?", seq, ref, kind)
                        : versions("WHERE external_id = ? AND kind = ? AND tombstone = false ORDER BY seq DESC LIMIT 1",
                                ref, kind);
                if (!rows.isEmpty()) {
                    return Optional.ofNullable(fromVersion(rows.get(0), false));
                }
                if (seq == null && SituationMemberTypes.INTEL_EVENT.equals(memberType)) {
                    return intelEvent(ref);
                }
                return Optional.empty();
            }
            case SituationMemberTypes.ALERT -> {
                long id;
                try {
                    id = Long.parseLong(ref.strip());
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
                return alerts("WHERE a.id = ?", false, id).stream().findFirst();
            }
            case SituationMemberTypes.INVESTIGATION -> {
                return investigation(ref);
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    // ------------------------------------------------------------------------- conversions

    /** The input of a version row, or null when it is not a linker input ({@code linkerOnly}) or unusable. */
    LinkerInput fromVersion(VersionRow row, boolean linkerOnly) {
        if (WindowEngine.SOURCE_ID.equals(row.sourceId()) && WindowEngine.ANOMALY_KIND.equals(row.kind())) {
            return anomaly(row, linkerOnly);
        }
        if (INTEL_KIND.equals(row.kind())) {
            return intelRecord(row, linkerOnly);
        }
        if (FINDING_KIND.equals(row.kind())) {
            return finding(row, linkerOnly);
        }
        return linkerOnly ? null : record(row);
    }

    private LinkerInput intelRecord(VersionRow row, boolean linkerOnly) {
        Map<String, Object> p = row.payload();
        String status = text(p, "status");
        if (linkerOnly && !INTEL_STATUSES.contains(status)) {
            return null;
        }
        String type = text(p, "type");
        String severity = Severities.normalize(text(p, "severity"));
        List<String> families = new ArrayList<>(new TreeSet<>(strings(p.get("signalFamilies"))));
        String family = families.isEmpty() ? "intel-event" : families.get(0);
        List<RecordRef> evidence = intelMemberRefs(row.externalId());
        if (evidence.isEmpty()) {
            evidence = List.of(new RecordRef(row.sourceId(), row.externalId(), null));
        }
        Region region = region(row.geom());
        boolean seed = "active".equals(status) && Severities.rank(severity) >= Severities.rank("warning");
        String theme = themes.forEventType(type);
        return new LinkerInput(row.seq(), SituationMemberTypes.INTEL_EVENT, row.externalId(), row.sourceId(),
                row.externalId(), row.version(), row.kind(), family, severity, text(p, "title"), humanize(type),
                row.observedAt(), row.geom(), region.codes(), region.name(), entities(withSelf(evidence, row)),
                text(p), theme, theme == null ? null : "table", seed, "core", evidence,
                DocInput.recordId(row.sourceId(), row.externalId()));
    }

    private LinkerInput finding(VersionRow row, boolean linkerOnly) {
        Map<String, Object> p = row.payload();
        String raw = text(p, "severity") == null ? "info" : text(p, "severity").toLowerCase(Locale.ROOT);
        if (linkerOnly && !FINDING_SEVERITIES.contains(raw)) {
            return null;
        }
        String type = text(p, "findingType");
        String family = text(p, "signalFamily") != null ? text(p, "signalFamily") : "analytic-finding";
        Region region = region(row.geom());
        String theme = themes.forFindingType(type);
        boolean seed = "significant".equals(raw) || "critical".equals(raw);
        List<RecordRef> evidence = new ArrayList<>();
        for (String ref : strings(p.get("evidenceRefs"))) {
            try {
                evidence.add(RecordRef.parse(ref.startsWith(RecordRef.PREFIX) ? ref : RecordRef.PREFIX + ref));
            } catch (RuntimeException e) {
                // Not a record ref: ignored for the roll-up.
            }
        }
        return new LinkerInput(row.seq(), SituationMemberTypes.FINDING, row.externalId(), row.sourceId(),
                row.externalId(), row.version(), row.kind(), family, Severities.normalize(raw), text(p, "title"),
                humanize(type), row.observedAt(), row.geom(), region.codes(), region.name(),
                entities(List.of(new RecordRef(row.sourceId(), row.externalId(), null))), text(p), theme,
                theme == null ? null : "table", seed, "core", evidence,
                DocInput.recordId(row.sourceId(), row.externalId()));
    }

    private LinkerInput anomaly(VersionRow row, boolean linkerOnly) {
        Map<String, Object> p = row.payload();
        String classification = text(p, "classification");
        if (linkerOnly && !ANOMALY_CLASSES.contains(classification)) {
            return null;
        }
        Set<String> codes = new TreeSet<>();
        String group = text(p, "groupKey");
        if (group != null && REGION_CODE.matcher(group).matches() && !"ZZ".equals(group)) {
            codes.add(group);
            if (group.length() > 2) {
                codes.add(group.substring(0, 2));
            }
        }
        String theme = themes.forTags(ruleTags(text(p, "ruleId")));
        String name = text(p, "name") == null ? "Anomaly" : text(p, "name");
        String title = name + (classification == null ? "" : " (" + classification + ")");
        return new LinkerInput(row.seq(), SituationMemberTypes.ANOMALY, row.sourceId() + "/" + row.externalId(),
                row.sourceId(), row.externalId(), row.version(), row.kind(), "activity-anomaly", "info", title,
                name, row.observedAt(), row.geom(), codes, null, Set.of(), title, theme,
                theme == null ? null : "table", false, "context", List.of(), null);
    }

    private LinkerInput record(VersionRow row) {
        Map<String, Object> p = row.payload();
        String text = text(p);
        ThemeTable.Theme theme = themes.forText(row.kind(), text, row.sourceId());
        Region region = region(row.geom());
        String title = firstText(p, "title", "name", "headline");
        List<RecordRef> evidence = List.of(new RecordRef(row.sourceId(), row.externalId(), null));
        return new LinkerInput(row.seq(), SituationMemberTypes.RECORD, row.sourceId() + "/" + row.externalId(),
                row.sourceId(), row.externalId(), row.version(), row.kind(), familyOf(row.kind()),
                Severities.normalize(text(p, "severity")), title == null ? row.kind() + " " + row.externalId() : title,
                humanize(row.kind()), row.observedAt(), row.geom(), region.codes(), region.name(), entities(evidence),
                text, theme.theme(), theme.source(), false, "core", evidence,
                DocInput.recordId(row.sourceId(), row.externalId()));
    }

    /** An intelligence event that has no published record (a private event): read from its table. */
    private Optional<LinkerInput> intelEvent(String id) {
        List<LinkerInput> out = new ArrayList<>();
        try {
            jdbc.query("SELECT id, type, title, severity, status, start_time, updated_at, ST_AsText(geom) AS wkt"
                    + " FROM intelligence_event WHERE id = ?", rs -> {
                        Geometry geom = wkt(rs.getString("wkt"));
                        Region region = region(geom);
                        List<RecordRef> evidence = intelMemberRefs(id);
                        String type = rs.getString("type");
                        String theme = themes.forEventType(type);
                        Instant observed = instant(rs, "updated_at");
                        out.add(new LinkerInput(0, SituationMemberTypes.INTEL_EVENT, id, null, id, null, INTEL_KIND,
                                "intel-event", Severities.normalize(rs.getString("severity")), rs.getString("title"),
                                humanize(type), observed == null ? instant(rs, "start_time") : observed, geom,
                                region.codes(), region.name(), entities(evidence), rs.getString("title"), theme,
                                theme == null ? null : "table", false, "core", evidence, null));
                    }, id);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        return out.stream().findFirst();
    }

    private Optional<LinkerInput> investigation(String ref) {
        List<LinkerInput> out = new ArrayList<>();
        try {
            jdbc.query("SELECT id, title, created_at FROM investigation WHERE id = ? OR display_id = ?", rs -> {
                out.add(new LinkerInput(0, SituationMemberTypes.INVESTIGATION, rs.getString("id"), null,
                        rs.getString("id"), null, "investigation", "investigation", "info", rs.getString("title"),
                        "Investigation", instant(rs, "created_at"), null, Set.of(), null, Set.of(),
                        rs.getString("title"), null, null, false, "context", List.of(), null));
            }, ref, ref);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        return out.stream().findFirst();
    }

    private List<LinkerInput> alerts(String where, boolean linkerOnly, Object... args) {
        List<LinkerInput> out = new ArrayList<>();
        jdbc.query("SELECT a.id, a.seq, a.source_id, a.external_id, a.kind, a.severity, a.observed_at, a.fired_at,"
                + " a.message, a.tags::text AS tags, a.region::text AS region, r.name AS rule_name,"
                + " (SELECT ST_AsText(v.geom) FROM record_version v WHERE v.seq = a.seq LIMIT 1) AS wkt"
                + " FROM alert a LEFT JOIN trigger_rule r ON r.id = a.rule_id " + where, rs -> {
                    out.add(alert(rs));
                }, args);
        return out;
    }

    private LinkerInput alert(ResultSet rs) throws SQLException {
        String source = rs.getString("source_id");
        String external = rs.getString("external_id");
        String kind = rs.getString("kind");
        String severity = Severities.normalize(rs.getString("severity"));
        List<String> tags = rs.getString("tags") == null ? List.of() : json.readValue(rs.getString("tags"), STRINGS);
        Geometry geom = wkt(rs.getString("wkt"));
        Region region = region(geom);
        Set<String> codes = new TreeSet<>(region.codes());
        String regionName = region.name();
        if (rs.getString("region") != null) {
            RegionTags tags2 = RegionTags.fromJson(rs.getString("region"));
            if (tags2 != null) {
                if (tags2.iso2() != null) {
                    codes.add(tags2.iso2());
                }
                if (tags2.admin1() != null) {
                    codes.add(tags2.admin1());
                }
                regionName = regionName != null ? regionName : name(tags2);
            }
        }
        String ruleName = rs.getString("rule_name");
        String message = rs.getString("message");
        String title = (ruleName == null ? "Alert" : ruleName) + (message == null ? "" : ": " + message);
        String text = title;
        String theme = themes.forTags(tags);
        String themeSource = theme == null ? null : "table";
        if (theme == null) {
            ThemeTable.Theme byText = themes.forText(kind, text, source);
            theme = byText.theme();
            themeSource = byText.source();
        }
        Instant observed = instant(rs, "observed_at");
        List<RecordRef> evidence = source == null || external == null ? List.of()
                : List.of(new RecordRef(source, external, null));
        return new LinkerInput(rs.getLong("seq"), SituationMemberTypes.ALERT, Long.toString(rs.getLong("id")), source,
                external, null, kind, familyOf(kind), severity, title, ruleName == null ? "Alert" : ruleName,
                observed == null ? instant(rs, "fired_at") : observed, geom, codes, regionName, entities(evidence),
                text, theme, themeSource, "critical".equals(severity), "core", evidence,
                source == null ? null : DocInput.recordId(source, external));
    }

    // ------------------------------------------------------------------------- helpers

    /** True when the input's record or any record it rests on comes from a private source (039). */
    public boolean isPrivate(LinkerInput input) {
        EgressPolicy policy = egress.getIfAvailable();
        if (policy == null) {
            return false;
        }
        try {
            if (input.sourceId() != null && policy.isPrivate(input.sourceId())) {
                return true;
            }
            for (RecordRef ref : input.evidenceRefs()) {
                if (policy.isPrivate(ref.sourceId())) {
                    return true;
                }
            }
            if (SituationMemberTypes.INTEL_EVENT.equals(input.memberType())) {
                List<Boolean> flags = jdbc.queryForList("SELECT private FROM intelligence_event WHERE id = ?",
                        Boolean.class, input.ref());
                return !flags.isEmpty() && Boolean.TRUE.equals(flags.get(0));
            }
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    /** The signal family of a record kind: {@link SignalFamilies#forKind}, or the kind itself when unknown. */
    static String familyOf(String kind) {
        if (kind == null) {
            return "unknown";
        }
        String family = SignalFamilies.forKind(kind);
        return SignalFamilies.CONFLICT.equals(family) && !CONFLICT_KINDS.contains(kind) ? kind : family;
    }

    /** "maritime-disruption" → "Maritime disruption". */
    static String humanize(String type) {
        if (type == null || type.isBlank()) {
            return "Situation";
        }
        String spaced = type.replace('-', ' ').replace('_', ' ').strip();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    /**
     * Region codes and a display name of a geometry.
     *
     * @param codes the country and first-level region codes
     * @param name the first-level region, country or sea name, or null
     */
    record Region(Set<String> codes, String name) {}

    private Region region(Geometry geom) {
        if (geom == null || geom.isEmpty()) {
            return new Region(Set.of(), null);
        }
        RegionTagger tagger = regions.getIfAvailable();
        if (tagger == null) {
            return new Region(Set.of(), null);
        }
        try {
            RegionTags tags = tagger.tag(geom);
            Set<String> codes = new TreeSet<>();
            if (tags.iso2() != null) {
                codes.add(tags.iso2());
            }
            if (tags.admin1() != null) {
                codes.add(tags.admin1());
            }
            return new Region(codes, name(tags));
        } catch (RuntimeException e) {
            return new Region(Set.of(), null);
        }
    }

    private static String name(RegionTags tags) {
        if (tags.admin1Name() != null && tags.countryName() != null) {
            return tags.admin1Name() + ", " + tags.countryName();
        }
        if (tags.countryName() != null) {
            return tags.countryName();
        }
        if (tags.sea() != null) {
            return tags.sea();
        }
        return tags.iso2();
    }

    private List<RecordRef> intelMemberRefs(String eventId) {
        List<RecordRef> out = new ArrayList<>();
        try {
            jdbc.query("SELECT source_id, external_id FROM intelligence_event_member WHERE event_id = ?"
                    + " AND role <> 'contradicting' ORDER BY id", rs -> {
                        out.add(new RecordRef(rs.getString(1), rs.getString(2), null));
                    }, eventId);
        } catch (RuntimeException e) {
            return List.of();
        }
        return out;
    }

    private static List<RecordRef> withSelf(List<RecordRef> refs, VersionRow row) {
        List<RecordRef> out = new ArrayList<>(refs);
        out.add(new RecordRef(row.sourceId(), row.externalId(), null));
        return out;
    }

    /** The open 098 entities of {@code refs}. */
    Set<String> entities(List<RecordRef> refs) {
        Set<String> out = new TreeSet<>();
        if (refs.isEmpty()) {
            return out;
        }
        List<String> sources = new ArrayList<>();
        List<String> externals = new ArrayList<>();
        for (RecordRef ref : refs) {
            sources.add(ref.sourceId());
            externals.add(ref.externalId());
        }
        try {
            jdbc.query("SELECT DISTINCT l.entity_id FROM entity_link l JOIN unnest(?::text[], ?::text[])"
                            + " AS r(source_id, external_id) ON r.source_id = l.source_id"
                            + " AND r.external_id = l.external_id WHERE l.valid_to IS NULL",
                    rs -> {
                        out.add(rs.getString(1));
                    }, sources.toArray(String[]::new), externals.toArray(String[]::new));
        } catch (RuntimeException e) {
            return out;
        }
        return out;
    }

    private List<String> ruleTags(String ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        try {
            List<String> rows = jdbc.queryForList("SELECT tags::text FROM trigger_rule WHERE id = CAST(? AS uuid)",
                    String.class, ruleId);
            return rows.isEmpty() || rows.get(0) == null ? List.of() : json.readValue(rows.get(0), STRINGS);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private List<VersionRow> versions(String where, Object... args) {
        List<VersionRow> out = new ArrayList<>();
        jdbc.query("SELECT " + VERSION_COLUMNS + " FROM record_version " + where, rs -> {
            String text = rs.getString("payload");
            Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
            out.add(new VersionRow(rs.getLong("seq"), rs.getString("source_id"), rs.getString("external_id"),
                    rs.getString("kind"), rs.getInt("version"), instant(rs, "observed_at"), wkt(rs.getString("wkt")),
                    payload));
        }, args);
        return out;
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    static Geometry wkt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Geometry geometry = new WKTReader(Geo.factory()).read(text);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(Map<String, Object> payload, String field) {
        Object value = payload == null ? null : payload.get(field);
        return value == null ? null : value.toString();
    }

    private static String firstText(Map<String, Object> payload, String... fields) {
        for (String field : fields) {
            String value = text(payload, field);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /** The text of a payload's text fields, joined. */
    static String text(Map<String, Object> payload) {
        Set<String> parts = new LinkedHashSet<>();
        for (String field : TEXT_FIELDS) {
            String value = text(payload, field);
            if (value != null && !value.isBlank()) {
                parts.add(value.strip());
            }
        }
        return String.join(" ", parts);
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    out.add(item.toString());
                }
            }
        }
        return out;
    }
}

package ai.foresignal.store.history;

import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.SqlParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * SQL fragments over {@code record_version} rows shared by the transaction-axis readers of prompt
 * 037 (asOf tiles, frames): the per-source validity table, the validity predicate at an instant,
 * the title resolved from the version payload and the tile properties of the version payload.
 *
 * <p>Every value is bound through {@link SqlParams}; only property names matching
 * {@link #PROPERTY} are interpolated.
 */
public final class VersionSql {

    /** A property name that may be interpolated (the tile property rule of prompt 016). */
    public static final Pattern PROPERTY = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    /** {@code jsonb_build_object} takes at most 100 arguments; properties are built in chunks. */
    static final int PAIRS_PER_OBJECT = 40;

    private VersionSql() {}

    /**
     * {@code (VALUES (:source, :ttl::interval), …)} over {@code sources} with each source's validity
     * from {@code plan} (TTL plus widening), to join as {@code s(source_id, ttl)}.
     */
    public static String validityValues(Collection<SourceDescriptor> sources, AsOfPlan plan, SqlParams p) {
        StringJoiner values = new StringJoiner(", ", "(VALUES ", ")");
        for (SourceDescriptor descriptor : sources) {
            Duration ttl = plan.validity(descriptor.id());
            values.add("(" + p.text(descriptor.id()) + ", CAST(" + p.add(ttl == null ? null : ttl.toMillis() + " milliseconds")
                    + " AS interval))");
        }
        return values.toString();
    }

    /**
     * True at {@code at} for the version aliased {@code v} with validity column {@code ttl}
     * (Core-Data-Model §12): TRACK by {@code ingested_at + ttl}, ignoring an explicit
     * {@code validUntil}; STANDARD by its explicit {@code validUntil}, else {@code ingested_at + ttl}.
     */
    public static String validAt(String v, String ttl, Instant at, SqlParams p) {
        String t = p.instant(at);
        return "(CASE WHEN " + v + ".history_tier = CAST(" + p.add((int) HistoryTier.TRACK.code()) + " AS smallint)"
                + " THEN (" + ttl + " IS NULL OR " + v + ".ingested_at + " + ttl + " > " + t + ")"
                + " ELSE (" + v + ".valid_until_explicit > " + t + " OR (" + v + ".valid_until_explicit IS NULL AND ("
                + ttl + " IS NULL OR " + v + ".ingested_at + " + ttl + " > " + t + "))) END)";
    }

    /**
     * The title of the version aliased {@code v}, resolved from its payload as
     * {@link TitleResolver} does: the source's {@code titleField}, then {@code title}, {@code name},
     * {@code callsign}, {@code place}; blank values skipped; at most 256 characters.
     */
    public static String title(String v, Collection<SourceDescriptor> sources, SqlParams p) {
        List<String> fallback = new ArrayList<>();
        for (String field : TitleResolver.FALLBACK_FIELDS) {
            fallback.add(field(v, field, p));
        }
        StringBuilder out = new StringBuilder("left(COALESCE(");
        boolean anyTitleField = sources.stream().anyMatch(d -> d.titleField() != null);
        if (anyTitleField) {
            out.append("CASE ").append(v).append(".source_id");
            for (SourceDescriptor descriptor : sources) {
                if (descriptor.titleField() != null) {
                    out.append(" WHEN ").append(p.text(descriptor.id())).append(" THEN ")
                            .append(field(v, descriptor.titleField(), p));
                }
            }
            out.append(" END, ");
        }
        out.append(String.join(", ", fallback)).append("), ").append(TitleResolver.MAX_TITLE_LENGTH).append(")");
        return out.toString();
    }

    /**
     * {@code jsonb_strip_nulls(jsonb_build_object('p', payload -> 'p', …))} over {@code properties}
     * (validated names), or {@code '{}'::jsonb} when there are none.
     */
    public static String tileProperties(String payload, Collection<String> properties) {
        List<String> parts = new ArrayList<>();
        List<String> pairs = new ArrayList<>();
        for (String property : properties) {
            if (!PROPERTY.matcher(property).matches()) {
                throw new IllegalArgumentException("invalid property name: " + property);
            }
            pairs.add("'" + property + "', " + payload + " -> '" + property + "'");
            if (pairs.size() == PAIRS_PER_OBJECT) {
                parts.add("jsonb_build_object(" + String.join(", ", pairs) + ")");
                pairs.clear();
            }
        }
        if (!pairs.isEmpty()) {
            parts.add("jsonb_build_object(" + String.join(", ", pairs) + ")");
        }
        return parts.isEmpty() ? "CAST('{}' AS jsonb)" : "jsonb_strip_nulls(" + String.join(" || ", parts) + ")";
    }

    /** A point for any geometry: the point itself, else {@code ST_PointOnSurface}; null for null. */
    public static String point(String geom) {
        return "(CASE WHEN " + geom + " IS NULL THEN NULL WHEN GeometryType(" + geom + ") = 'POINT' THEN " + geom
                + " ELSE ST_PointOnSurface(" + geom + ") END)";
    }

    private static String field(String v, String name, SqlParams p) {
        return "NULLIF(btrim(" + v + ".payload ->> " + p.text(name) + "), '')";
    }
}

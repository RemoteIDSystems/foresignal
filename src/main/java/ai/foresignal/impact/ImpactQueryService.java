package ai.foresignal.impact;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.impact.rate.ImpactRateService;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads projected impacts and conditional rates for the API and MCP (prompt 127 §5).
 *
 * <ul>
 *   <li><b>List:</b> the current (open) {@code projected-impact} findings, grouped by hazard and highest probability
 *       first within each hazard (keyset on hazard key, {@code pImpact} descending and id), filtered by hazard type,
 *       region (an admin1 code, or a country code matching its admin1 regions), {@code detectedAt ≥ since} and a
 *       minimum {@code pImpact}. Private findings are listed only for callers with private access.
 *   <li><b>Detail:</b> any projection, current or superseded, with its hazard geometries; {@code 404 UNKNOWN_IMPACT}
 *       otherwise.
 *   <li><b>Rates:</b> the built-in pairs' rates, paged by position.
 * </ul>
 */
@Service
public class ImpactQueryService {

    /** A region filter: a country or admin1 code. */
    public static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private final NamedParameterJdbcTemplate jdbc;
    private final AnalyticFindingRepository findings;
    private final ImpactRateService rates;

    /** Creates the service. */
    public ImpactQueryService(NamedParameterJdbcTemplate jdbc, AnalyticFindingRepository findings,
            ImpactRateService rates) {
        this.jdbc = jdbc;
        this.findings = findings;
        this.rates = rates;
    }

    /**
     * A page.
     *
     * @param items the items
     * @param nextCursor the next cursor, or null
     */
    public record Page<T>(List<T> items, String nextCursor) {}

    /**
     * The list filters.
     *
     * @param hazardType a hazard type, or null
     * @param region a country or admin1 code, or null
     * @param since {@code detectedAt ≥ since}, or null
     * @param minProbability the minimum {@code pImpact}, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     */
    public record Query(String hazardType, String region, Instant since, Double minProbability, String cursor,
            int limit) {}

    /** The current projections matching the query. */
    @Transactional(readOnly = true)
    public Page<AnalyticFindingEntity> list(Query q, boolean seesPrivate) {
        StringBuilder sql = new StringBuilder("SELECT id, coalesce(stats ->> 'hazardKey', '') AS hk, CAST(stats ->>"
                + " 'pImpact' AS double precision) AS p FROM analytic_finding WHERE finding_type = :type AND status ="
                + " 'open'");
        MapSqlParameterSource p = new MapSqlParameterSource("type", ImpactProjectionService.FINDING_TYPE);
        if (!seesPrivate) {
            sql.append(" AND private = false");
        }
        if (q.hazardType() != null) {
            sql.append(" AND stats ->> 'hazardType' = :hazardType");
            p.addValue("hazardType", q.hazardType());
        }
        if (q.region() != null) {
            sql.append(" AND (stats ->> 'admin1' = :region OR stats ->> 'admin1' LIKE :regionPrefix)");
            p.addValue("region", q.region());
            p.addValue("regionPrefix", q.region().length() == 2 ? q.region() + "-%" : q.region());
        }
        if (q.since() != null) {
            sql.append(" AND detected_at >= :since");
            p.addValue("since", Timestamp.from(q.since()));
        }
        if (q.minProbability() != null) {
            sql.append(" AND CAST(stats ->> 'pImpact' AS double precision) >= :minP");
            p.addValue("minP", q.minProbability());
        }
        String[] after = decode(q.cursor());
        if (after != null) {
            sql.append(" AND (coalesce(stats ->> 'hazardKey', '') > :afterKey OR (coalesce(stats ->> 'hazardKey', '') ="
                    + " :afterKey AND (CAST(stats ->> 'pImpact' AS double precision) < :afterP OR (CAST(stats ->>"
                    + " 'pImpact' AS double precision) = :afterP AND id > :afterId))))");
            p.addValue("afterKey", after[0]);
            p.addValue("afterP", Double.parseDouble(after[1]));
            p.addValue("afterId", after[2]);
        }
        sql.append(" ORDER BY hk ASC, p DESC, id ASC LIMIT :limit");
        p.addValue("limit", q.limit() + 1);
        List<String[]> keys = jdbc.query(sql.toString(), p, (rs, n) -> new String[] {rs.getString("id"),
            rs.getString("hk"), Double.toString(rs.getDouble("p"))});
        List<AnalyticFindingEntity> items = new ArrayList<>();
        for (int i = 0; i < keys.size() && i < q.limit(); i++) {
            findings.findById(keys.get(i)[0]).ifPresent(items::add);
        }
        String next = null;
        if (keys.size() > q.limit()) {
            String[] last = keys.get(q.limit() - 1);
            next = encode(last[1], last[2], last[0]);
        }
        return new Page<>(items, next);
    }

    /**
     * One projection.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_IMPACT}
     */
    @Transactional(readOnly = true)
    public AnalyticFindingEntity get(String findingId, boolean seesPrivate) {
        AnalyticFindingEntity f = findingId == null ? null : findings.findById(findingId).orElse(null);
        if (f == null || !ImpactProjectionService.FINDING_TYPE.equals(f.getFindingType())
                || (f.isPrivateFinding() && !seesPrivate)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_IMPACT, "No projected impact '" + findingId + "'.");
        }
        return f;
    }

    /**
     * The rates of the built-in pairs matching the filters, paged by position.
     *
     * @param hazardClass a hazard class, or null
     * @param impactFamily an impact family, or null
     * @param regionClass a region class, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     */
    public Page<Map<String, Object>> rates(String hazardClass, String impactFamily, String regionClass, String cursor,
            int limit) {
        int offset = 0;
        if (cursor != null) {
            try {
                offset = Integer.parseInt(new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("cursor", "is not a cursor of this list");
            }
            if (offset < 0) {
                throw ValidationException.forField("cursor", "is not a cursor of this list");
            }
        }
        List<ImpactRateService.ImpactRate> all = rates.list(hazardClass, impactFamily, regionClass);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = offset; i < all.size() && items.size() < limit; i++) {
            items.add(all.get(i).view());
        }
        String next = offset + limit < all.size() ? Base64.getUrlEncoder().withoutPadding().encodeToString(
                Integer.toString(offset + limit).getBytes(StandardCharsets.UTF_8)) : null;
        return new Page<>(items, next);
    }

    static String encode(String hazardKey, String p, String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((hazardKey + "\n" + p + "\n" + id)
                .getBytes(StandardCharsets.UTF_8));
    }

    static String[] decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8).split("\n", -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException("parts");
            }
            Double.parseDouble(parts[1]);
            return parts;
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor of this list");
        }
    }
}

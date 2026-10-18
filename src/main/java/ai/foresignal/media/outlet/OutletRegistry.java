package ai.foresignal.media.outlet;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsNames;
import ai.foresignal.source.formats.news.NewsOutletLookup;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The news outlet registry (prompt 133, {@code media_outlet}, {@code media_rating}, {@code outlet_consensus}).
 *
 * <ul>
 *   <li><strong>Domains:</strong> an article's domain resolves to the outlet listing it, else to the outlet of a parent
 *       domain ({@code edition.cnn.com} → {@code cnn.com}); a merged outlet resolves to its survivor. An unknown domain
 *       is added as an {@code unrated} outlet of type {@code other} with the country and language the article gave
 *       ({@link #resolve}).
 *   <li><strong>Wires:</strong> the news agencies of {@code media/wires.json} are seeded once, type {@code wire}, each
 *       mapped to its own 116 originator; other outlets may list the wires they syndicate in {@code aggregates} (none
 *       listed means any wire).
 *   <li><strong>Ratings and consensus:</strong> rows written only by {@link RatingImportService}; the consensus of each
 *       dimension over the enabled providers is recomputed after every import ({@link Consensus}).
 *   <li><strong>Edits:</strong> {@code PATCH} sets the name, type, owner, parent, Wikidata QID, domains and syndicated
 *       wires, and merges duplicate outlets (domains and ratings move; a rating the survivor already has from the same
 *       provider on the same dimension is kept). Ratings are never edited here.
 * </ul>
 */
@Component
public class OutletRegistry implements NewsOutletLookup {

    private static final Logger log = LoggerFactory.getLogger(OutletRegistry.class);

    /** The bundled wires. */
    public static final String WIRES_RESOURCE = "media/wires.json";

    /** The largest page. */
    public static final int MAX_LIMIT = 500;

    /** The default page. */
    public static final int DEFAULT_LIMIT = 100;

    private static final Pattern DOMAIN = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]"
            + "{0,61}[a-z0-9])?)+$");
    private static final Pattern QID = Pattern.compile("^Q[0-9]+$");

    private static final String COLUMNS = "id, name, primary_domain, domains, country, languages, type, owner_org,"
            + " parent_org, wikidata_qid, originator_id, aggregates, rated, merged_into, wikidata_checked_at,"
            + " created_at, updated_at";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the registry. */
    public OutletRegistry(JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * An outlet.
     *
     * @param id the id
     * @param name the name
     * @param primaryDomain the domain it was created from
     * @param domains every domain
     * @param country the ISO 3166-1 alpha-2 country, or null
     * @param languages the language codes
     * @param type the outlet type
     * @param ownerOrg the owning organization, or null
     * @param parentOrg the parent organization, or null
     * @param wikidataQid the Wikidata item, or null
     * @param originatorId the 116 originator (wires), or null
     * @param aggregates the wire originators it syndicates (empty: any wire)
     * @param rated true when any provider rated it
     * @param mergedInto the survivor of a merge, or null
     * @param wikidataCheckedAt when Wikidata was last asked about it, or null
     * @param createdAt the creation time
     * @param updatedAt the last change
     */
    public record Outlet(String id, String name, String primaryDomain, List<String> domains, String country,
            List<String> languages, String type, String ownerOrg, String parentOrg, String wikidataQid,
            String originatorId, List<String> aggregates, boolean rated, String mergedInto, Instant wikidataCheckedAt,
            Instant createdAt, Instant updatedAt) {

        /** Copies the lists. */
        public Outlet {
            domains = domains == null ? List.of() : List.copyOf(domains);
            languages = languages == null ? List.of() : List.copyOf(languages);
            aggregates = aggregates == null ? List.of() : List.copyOf(aggregates);
        }

        /** True for a news agency. */
        public boolean wire() {
            return MediaVocabulary.WIRE.equals(type);
        }

        /** True when this outlet may syndicate {@code wireOriginator}: it lists it, or lists no wire at all. */
        public boolean syndicates(String wireOriginator) {
            return !wire() && (aggregates.isEmpty() || aggregates.contains(wireOriginator));
        }
    }

    /**
     * A stored rating.
     *
     * @param outletId the outlet
     * @param provider the provider
     * @param dimension the dimension
     * @param valueNorm the normalized value, or null
     * @param valueRaw the provider's raw value, or null
     * @param label the provider's label, or null
     * @param ratedAt the provider's rating date, or null
     * @param license the license
     * @param attribution the attribution
     * @param url the rating's link, or null
     * @param egress {@code OPEN} or {@code LOCAL_ONLY}
     * @param importedAt when it was imported
     */
    public record Rating(String outletId, String provider, String dimension, Double valueNorm, String valueRaw,
            String label, Instant ratedAt, String license, String attribution, String url, String egress,
            Instant importedAt) {}

    /**
     * One page of outlets.
     *
     * @param items the outlets
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<Outlet> items, String nextCursor) {}

    // ------------------------------------------------------------------------- seeding

    /** Seeds the bundled wires once (an outlet already holding a wire's domain is left as it is). */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            seedWires();
        } catch (RuntimeException e) {
            log.warn("Could not seed the news wires: {}", e.toString());
        }
    }

    /** Inserts the bundled wires whose primary domain is not in the registry. */
    public void seedWires() {
        JsonNode root;
        try (InputStream in = new ClassPathResource(WIRES_RESOURCE).getInputStream()) {
            root = json.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + WIRES_RESOURCE, e);
        }
        Instant now = clock.instant();
        for (JsonNode o : root.path("outlets")) {
            List<String> domains = new ArrayList<>();
            for (JsonNode d : o.path("domains")) {
                domains.add(ArticleUrls.normalizeDomain(d.asString()));
            }
            List<String> languages = new ArrayList<>();
            for (JsonNode l : o.path("languages")) {
                languages.add(l.asString());
            }
            if (domains.isEmpty() || byDomain(domains.get(0)).isPresent()) {
                continue;
            }
            jdbc.update("INSERT INTO media_outlet (id, name, primary_domain, domains, country, languages, type,"
                            + " originator_id, aggregates, rated, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?,"
                            + " 'wire', ?, '{}', false, ?, ?) ON CONFLICT (primary_domain) DO NOTHING",
                    Ulids.next(now), o.path("name").asString(), domains.get(0), domains.toArray(String[]::new),
                    o.path("country").asString(null), languages.toArray(String[]::new),
                    o.path("originator").asString(null), Timestamp.from(now), Timestamp.from(now));
        }
    }

    // ------------------------------------------------------------------------- lookups

    @Override
    public String resolve(String domain, String countryIso2, String language) {
        String d = ArticleUrls.normalizeDomain(domain);
        if (d == null) {
            return null;
        }
        Optional<Outlet> known = byDomain(d);
        if (known.isPresent()) {
            return known.get().id();
        }
        return register(d, countryIso2, language).id();
    }

    /** The outlet of {@code domain} (or of a parent domain), following merges. */
    public Optional<Outlet> byDomain(String domain) {
        String d = ArticleUrls.normalizeDomain(domain);
        if (d == null) {
            return Optional.empty();
        }
        String candidate = d;
        while (candidate != null && candidate.contains(".")) {
            List<Outlet> rows = jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE ? = ANY(domains)"
                    + " ORDER BY merged_into NULLS FIRST, created_at, id LIMIT 1", (rs, i) -> outlet(rs), candidate);
            if (!rows.isEmpty()) {
                return Optional.of(follow(rows.get(0)));
            }
            int dot = candidate.indexOf('.');
            String parent = candidate.substring(dot + 1);
            candidate = parent.contains(".") ? parent : null;
        }
        return Optional.empty();
    }

    /** Registers an {@code unrated} outlet for an unknown domain (idempotent). */
    @Transactional
    public Outlet register(String domain, String countryIso2, String language) {
        String d = ArticleUrls.normalizeDomain(domain);
        Instant now = clock.instant();
        String country = NewsNames.countryIso2(countryIso2);
        String lang = NewsNames.languageCode(language);
        jdbc.update("INSERT INTO media_outlet (id, name, primary_domain, domains, country, languages, type, aggregates,"
                        + " rated, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, 'other', '{}', false, ?, ?)"
                        + " ON CONFLICT (primary_domain) DO NOTHING", Ulids.next(now), d, d, new String[] {d}, country,
                lang == null ? new String[0] : new String[] {lang}, Timestamp.from(now), Timestamp.from(now));
        return jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE primary_domain = ?", (rs, i) -> outlet(rs), d)
                .stream().findFirst().map(this::follow).orElseThrow();
    }

    /**
     * One outlet (a merged id answers with its survivor).
     *
     * @throws NotFoundException 404 {@code UNKNOWN_OUTLET}
     */
    public Outlet get(String id) {
        return find(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_OUTLET, "No outlet '" + id + "'."));
    }

    /** One outlet, following merges, or empty. */
    public Optional<Outlet> find(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE id = ?", (rs, i) -> outlet(rs), id).stream()
                .findFirst().map(this::follow);
    }

    /** Outlets by id (merged ones resolved to their survivors), keyed by the asked id. */
    public Map<String, Outlet> byIds(Collection<String> ids) {
        Map<String, Outlet> out = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        for (Outlet o : jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE id = ANY(?)", (rs, i) -> outlet(rs),
                (Object) new LinkedHashSet<>(ids).toArray(String[]::new))) {
            out.put(o.id(), follow(o));
        }
        return out;
    }

    /** Every outlet of type {@code wire} that is not merged. */
    public List<Outlet> wires() {
        return jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE type = 'wire' AND merged_into IS NULL"
                + " ORDER BY id", (rs, i) -> outlet(rs));
    }

    /** Outlets not merged, with a Wikidata QID or none checked since {@code before}, at most {@code limit}. */
    public List<Outlet> dueForWikidata(Instant before, int limit) {
        return jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE merged_into IS NULL AND (wikidata_checked_at"
                + " IS NULL OR wikidata_checked_at < ?) ORDER BY wikidata_checked_at NULLS FIRST, id LIMIT ?",
                (rs, i) -> outlet(rs), Timestamp.from(before), limit);
    }

    /**
     * Lists outlets (not merged), keyset on id.
     *
     * @param q text matched against name and domains, or null
     * @param country an ISO country, or null
     * @param type an outlet type, or null
     * @param bucket a lean bucket ({@code unrated}: no lean consensus), or null
     */
    public Page list(String q, String country, String type, String bucket, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM media_outlet o WHERE merged_into IS NULL");
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (lower(name) LIKE ? OR EXISTS (SELECT 1 FROM unnest(domains) d WHERE d LIKE ?))");
            String like = "%" + q.strip().toLowerCase(Locale.ROOT).replace("%", "").replace("_", "") + "%";
            args.add(like);
            args.add(like);
        }
        if (country != null && !country.isBlank()) {
            sql.append(" AND country = ?");
            args.add(country.strip().toUpperCase(Locale.ROOT));
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            args.add(type);
        }
        if (bucket != null && !bucket.isBlank()) {
            if (MediaVocabulary.UNRATED.equals(bucket)) {
                sql.append(" AND NOT EXISTS (SELECT 1 FROM outlet_consensus c WHERE c.outlet_id = o.id AND c.dimension"
                        + " = 'lean' AND c.bucket IS NOT NULL)");
            } else {
                sql.append(" AND EXISTS (SELECT 1 FROM outlet_consensus c WHERE c.outlet_id = o.id AND c.dimension"
                        + " = 'lean' AND c.bucket = ?)");
                args.add(bucket);
            }
        }
        if (cursor != null && !cursor.isBlank()) {
            sql.append(" AND id > ?");
            args.add(cursor);
        }
        sql.append(" ORDER BY id LIMIT ?");
        args.add(limit + 1);
        List<Outlet> rows = jdbc.query(sql.toString(), (rs, i) -> outlet(rs), args.toArray());
        String next = null;
        if (rows.size() > limit) {
            rows = rows.subList(0, limit);
            next = rows.get(rows.size() - 1).id();
        }
        return new Page(rows, next);
    }

    // ------------------------------------------------------------------------- ratings and consensus

    /** The ratings of an outlet, by provider then dimension. */
    public List<Rating> ratings(String outletId) {
        return jdbc.query("SELECT outlet_id, provider, dimension, value_norm, value_raw, label, rated_at, licence,"
                + " attribution, url, egress, imported_at FROM media_rating WHERE outlet_id = ?"
                + " ORDER BY provider, dimension", (rs, i) -> rating(rs), outletId);
    }

    /**
     * Inserts or replaces one rating.
     *
     * @return true when it was new, false when it replaced an earlier one
     */
    public boolean upsertRating(Rating r) {
        int updated = jdbc.update("UPDATE media_rating SET value_norm = ?, value_raw = ?, label = ?, rated_at = ?,"
                        + " licence = ?, attribution = ?, url = ?, egress = ?, imported_at = ? WHERE outlet_id = ?"
                        + " AND provider = ? AND dimension = ?", r.valueNorm(), clip(r.valueRaw(), 500),
                clip(r.label(), 300), ts(r.ratedAt()), clip(r.license(), 300), clip(r.attribution(), 500),
                clip(r.url(), 2000), r.egress(), Timestamp.from(r.importedAt()), r.outletId(), r.provider(),
                r.dimension());
        if (updated > 0) {
            return false;
        }
        jdbc.update("INSERT INTO media_rating (id, outlet_id, provider, dimension, value_norm, value_raw, label,"
                        + " rated_at, licence, attribution, url, egress, imported_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,"
                        + " ?, ?, ?, ?)", Ulids.next(r.importedAt()), r.outletId(), r.provider(), r.dimension(),
                r.valueNorm(), clip(r.valueRaw(), 500), clip(r.label(), 300), ts(r.ratedAt()), clip(r.license(), 300),
                clip(r.attribution(), 500), clip(r.url(), 2000), r.egress(), Timestamp.from(r.importedAt()));
        jdbc.update("UPDATE media_outlet SET rated = true, updated_at = ? WHERE id = ?", Timestamp.from(r.importedAt()),
                r.outletId());
        return true;
    }

    /** Recomputes and stores every dimension's consensus of one outlet over {@code enabledProviders}. */
    public List<Consensus.Result> recomputeConsensus(String outletId, Set<String> enabledProviders) {
        Map<String, List<Consensus.ProviderValue>> byDimension = new LinkedHashMap<>();
        for (Rating r : ratings(outletId)) {
            if (!enabledProviders.contains(r.provider())) {
                continue;
            }
            byDimension.computeIfAbsent(r.dimension(), k -> new ArrayList<>()).add(new Consensus.ProviderValue(
                    r.provider(), r.valueNorm(), r.label(), r.ratedAt(), r.license(), r.egress()));
        }
        Instant now = clock.instant();
        jdbc.update("DELETE FROM outlet_consensus WHERE outlet_id = ?", outletId);
        List<Consensus.Result> out = new ArrayList<>();
        for (Map.Entry<String, List<Consensus.ProviderValue>> e : byDimension.entrySet()) {
            Consensus.Result c = Consensus.compute(e.getKey(), e.getValue());
            List<Map<String, Object>> providers = new ArrayList<>();
            for (Consensus.ProviderValue v : c.providers()) {
                Map<String, Object> m = v.toMap();
                m.put("egress", v.egress());
                providers.add(m);
            }
            jdbc.update("INSERT INTO outlet_consensus (id, outlet_id, dimension, median, n_providers, disagreement,"
                            + " bucket, providers, computed_at) VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?)",
                    outletId + "|" + c.dimension(), outletId, c.dimension(), c.median(), c.nProviders(),
                    c.disagreement(), c.bucket(), json.writeValueAsString(providers), Timestamp.from(now));
            out.add(c);
        }
        return out;
    }

    /** Recomputes the consensus of every outlet with a rating. */
    public int recomputeAll(Set<String> enabledProviders) {
        List<String> ids = jdbc.queryForList("SELECT DISTINCT outlet_id FROM media_rating", String.class);
        jdbc.update("DELETE FROM outlet_consensus WHERE outlet_id NOT IN (SELECT outlet_id FROM media_rating)");
        for (String id : ids) {
            recomputeConsensus(id, enabledProviders);
        }
        return ids.size();
    }

    /** The stored consensus of several outlets: outlet id → dimension → result (full, every egress). */
    public Map<String, Map<String, Consensus.Result>> consensus(Collection<String> outletIds) {
        Map<String, Map<String, Consensus.Result>> out = new LinkedHashMap<>();
        if (outletIds == null || outletIds.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT outlet_id, dimension, median, n_providers, disagreement, bucket, providers::text AS providers"
                + " FROM outlet_consensus WHERE outlet_id = ANY(?) ORDER BY outlet_id, dimension", rs -> {
                    List<Consensus.ProviderValue> providers = new ArrayList<>();
                    JsonNode list = json.readTree(rs.getString("providers") == null ? "[]" : rs.getString("providers"));
                    for (JsonNode p : list) {
                        providers.add(new Consensus.ProviderValue(p.path("provider").asString(null),
                                p.hasNonNull("valueNorm") ? p.get("valueNorm").asDouble() : null,
                                p.path("label").asString(null), parse(p.path("ratedAt").asString(null)),
                                p.path("licence").asString(null), p.path("egress").asString(MediaVocabulary.OPEN)));
                    }
                    out.computeIfAbsent(rs.getString("outlet_id"), k -> new LinkedHashMap<>()).put(
                            rs.getString("dimension"), new Consensus.Result(rs.getString("dimension"),
                                    (Double) rs.getObject("median"), rs.getInt("n_providers"),
                                    (Double) rs.getObject("disagreement"), rs.getString("bucket"), providers));
                }, (Object) new LinkedHashSet<>(outletIds).toArray(String[]::new));
        return out;
    }

    /** The stored consensus of one outlet, by dimension. */
    public Map<String, Consensus.Result> consensus(String outletId) {
        return consensus(List.of(outletId)).getOrDefault(outletId, Map.of());
    }

    // ------------------------------------------------------------------------- Wikidata facts

    /**
     * Records registry facts Wikidata gave (not ratings): the QID, country when unknown, owner and parent organization
     * names, and a type when the outlet's is still {@code other}.
     */
    public void applyWikidataFacts(String outletId, String qid, String country, String ownerOrg, String parentOrg,
            String type) {
        Instant now = clock.instant();
        jdbc.update("UPDATE media_outlet SET wikidata_qid = COALESCE(wikidata_qid, ?), country = COALESCE(country, ?),"
                        + " owner_org = COALESCE(?, owner_org), parent_org = COALESCE(?, parent_org),"
                        + " type = CASE WHEN type = 'other' AND ? IS NOT NULL THEN ? ELSE type END, updated_at = ?"
                        + " WHERE id = ?", qid, country, clip(ownerOrg, 300), clip(parentOrg, 300), type, type,
                Timestamp.from(now), outletId);
    }

    /** Marks outlets as asked of Wikidata at {@code at}. */
    public void markWikidataChecked(Collection<String> outletIds, Instant at) {
        if (outletIds.isEmpty()) {
            return;
        }
        jdbc.update("UPDATE media_outlet SET wikidata_checked_at = ? WHERE id = ANY(?)", Timestamp.from(at),
                (Object) outletIds.toArray(String[]::new));
    }

    // ------------------------------------------------------------------------- edits

    /**
     * Applies a {@code MediaOutletPatch} (JSON merge patch).
     *
     * @throws ValidationException 400 for invalid members
     * @throws NotFoundException 404 {@code UNKNOWN_OUTLET}
     */
    @Transactional
    public Outlet patch(String id, JsonNode body) {
        Outlet outlet = get(id);
        if (body == null || !body.isObject()) {
            throw new ValidationException("The outlet patch must be a JSON object.", List.of(new FieldError("body",
                    "must be a JSON object")));
        }
        List<FieldError> errors = new ArrayList<>();
        Set<String> allowed = Set.of("name", "type", "ownerOrg", "parentOrg", "wikidataQid", "domains", "aggregates",
                "mergeOutletIds");
        for (String name : body.propertyNames()) {
            if (!allowed.contains(name)) {
                errors.add(new FieldError(name, "is not a patchable member (ratings come only from providers)"));
            }
        }
        String name = outlet.name();
        if (body.has("name")) {
            String n = body.get("name").isString() ? body.get("name").asString().strip() : "";
            if (n.isEmpty() || n.length() > 200) {
                errors.add(new FieldError("name", "must be 1 to 200 characters"));
            }
            name = n;
        }
        String type = outlet.type();
        if (body.has("type")) {
            String t = body.get("type").asString(null);
            if (t == null || !MediaVocabulary.OUTLET_TYPES.contains(t)) {
                errors.add(new FieldError("type", "must be one of " + MediaVocabulary.OUTLET_TYPES));
            }
            type = t;
        }
        String owner = body.has("ownerOrg") ? nullableText(body.get("ownerOrg")) : outlet.ownerOrg();
        String parent = body.has("parentOrg") ? nullableText(body.get("parentOrg")) : outlet.parentOrg();
        String qid = outlet.wikidataQid();
        if (body.has("wikidataQid")) {
            qid = nullableText(body.get("wikidataQid"));
            if (qid != null && !QID.matcher(qid).matches()) {
                errors.add(new FieldError("wikidataQid", "must match ^Q[0-9]+$"));
            }
        }
        List<String> domains = outlet.domains();
        if (body.has("domains")) {
            domains = domainList(body.get("domains"), "domains", errors);
            if (domains.isEmpty()) {
                errors.add(new FieldError("domains", "must list at least one domain"));
            }
        }
        List<String> aggregates = outlet.aggregates();
        if (body.has("aggregates")) {
            aggregates = new ArrayList<>();
            if (!body.get("aggregates").isArray()) {
                errors.add(new FieldError("aggregates", "must be an array of wire originator ids"));
            } else {
                for (JsonNode a : body.get("aggregates")) {
                    aggregates.add(a.asString());
                }
            }
        }
        List<String> merges = new ArrayList<>();
        if (body.has("mergeOutletIds")) {
            if (!body.get("mergeOutletIds").isArray()) {
                errors.add(new FieldError("mergeOutletIds", "must be an array of outlet ids"));
            } else {
                for (JsonNode m : body.get("mergeOutletIds")) {
                    String other = m.asString();
                    if (other.equals(outlet.id())) {
                        errors.add(new FieldError("mergeOutletIds", "cannot merge an outlet into itself"));
                    } else if (find(other).isEmpty()) {
                        errors.add(new FieldError("mergeOutletIds", "names no outlet: " + other));
                    } else {
                        merges.add(other);
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The outlet patch is not valid.", errors);
        }
        Instant now = clock.instant();
        Set<String> allDomains = new LinkedHashSet<>(domains);
        for (String other : merges) {
            Outlet loser = get(other);
            allDomains.addAll(loser.domains());
            merge(loser.id(), outlet.id(), now);
        }
        jdbc.update("UPDATE media_outlet SET name = ?, type = ?, owner_org = ?, parent_org = ?, wikidata_qid = ?,"
                        + " domains = ?, aggregates = ?, updated_at = ? WHERE id = ?", name, type, clip(owner, 300),
                clip(parent, 300), qid, allDomains.toArray(String[]::new), aggregates.toArray(String[]::new),
                Timestamp.from(now), outlet.id());
        return get(outlet.id());
    }

    private void merge(String loser, String survivor, Instant now) {
        jdbc.update("DELETE FROM media_rating l WHERE l.outlet_id = ? AND EXISTS (SELECT 1 FROM media_rating s"
                + " WHERE s.outlet_id = ? AND s.provider = l.provider AND s.dimension = l.dimension)", loser, survivor);
        jdbc.update("UPDATE media_rating SET outlet_id = ? WHERE outlet_id = ?", survivor, loser);
        jdbc.update("DELETE FROM outlet_consensus WHERE outlet_id = ?", loser);
        jdbc.update("UPDATE media_outlet SET merged_into = ?, domains = '{}', updated_at = ? WHERE id = ?", survivor,
                Timestamp.from(now), loser);
        jdbc.update("UPDATE media_outlet SET merged_into = ?, updated_at = ? WHERE merged_into = ?", survivor,
                Timestamp.from(now), loser);
        jdbc.update("UPDATE media_outlet SET rated = EXISTS (SELECT 1 FROM media_rating r WHERE r.outlet_id = ?)"
                + " WHERE id = ?", survivor, survivor);
        try {
            jdbc.update("UPDATE news_article SET outlet_id = ? WHERE outlet_id = ?", survivor, loser);
        } catch (RuntimeException e) {
            log.debug("No news articles to move from {}: {}", loser, e.toString());
        }
    }

    // ------------------------------------------------------------------------- mapping

    private Outlet follow(Outlet o) {
        Outlet current = o;
        for (int i = 0; i < 8 && current.mergedInto() != null; i++) {
            String next = current.mergedInto();
            List<Outlet> rows = jdbc.query("SELECT " + COLUMNS + " FROM media_outlet WHERE id = ?",
                    (rs, n) -> outlet(rs), next);
            if (rows.isEmpty()) {
                break;
            }
            current = rows.get(0);
        }
        return current;
    }

    private static List<String> domainList(JsonNode node, String field, List<FieldError> errors) {
        List<String> out = new ArrayList<>();
        if (node == null || !node.isArray()) {
            errors.add(new FieldError(field, "must be an array of domains"));
            return out;
        }
        for (JsonNode d : node) {
            String domain = ArticleUrls.normalizeDomain(d.asString(null));
            if (domain == null || !DOMAIN.matcher(domain).matches()) {
                errors.add(new FieldError(field, "is not a domain: " + d.asString(null)));
            } else if (!out.contains(domain)) {
                out.add(domain);
            }
        }
        return out;
    }

    private static String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String s = node.asString(null);
        return s == null || s.isBlank() ? null : s.strip();
    }

    static Outlet outlet(ResultSet rs) throws SQLException {
        return new Outlet(rs.getString("id"), rs.getString("name"), rs.getString("primary_domain"),
                strings(rs.getArray("domains")), rs.getString("country"), strings(rs.getArray("languages")),
                rs.getString("type"), rs.getString("owner_org"), rs.getString("parent_org"),
                rs.getString("wikidata_qid"), rs.getString("originator_id"), strings(rs.getArray("aggregates")),
                rs.getBoolean("rated"), rs.getString("merged_into"), instant(rs, "wikidata_checked_at"),
                instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private static Rating rating(ResultSet rs) throws SQLException {
        return new Rating(rs.getString("outlet_id"), rs.getString("provider"), rs.getString("dimension"),
                (Double) rs.getObject("value_norm"), rs.getString("value_raw"), rs.getString("label"),
                instant(rs, "rated_at"), rs.getString("licence"), rs.getString("attribution"), rs.getString("url"),
                rs.getString("egress"), instant(rs, "imported_at"));
    }

    static List<String> strings(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object o : (Object[]) array.getArray()) {
            if (o != null) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime v = rs.getObject(column, OffsetDateTime.class);
        return v == null ? null : v.toInstant();
    }

    private static Instant parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Timestamp ts(Instant i) {
        return i == null ? null : Timestamp.from(i);
    }

    private static String clip(String text, int max) {
        return text == null ? null : text.length() <= max ? text : text.substring(0, max);
    }
}

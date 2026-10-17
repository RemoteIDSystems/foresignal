package ai.foresignal.baserate;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.baserate.reference.ReferenceImportService;
import ai.foresignal.manifest.CanonicalJson;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The base-rate library (prompt 121, Intelligence-Design §7.1).
 *
 * <ul>
 *   <li><strong>Computation</strong> reads {@code reference_history} (imports and the own-history fold), builds each
 *       source's coverage from its imports' completed chunks (and the fold's span), and hands both to the pure
 *       {@link BaseRateCalculator}. Every rate states its reference class, period, windows, events, Jeffreys interval,
 *       source mix and licenses, and carries the caveats {@code SMALL_SAMPLE} (fewer than {@value #SMALL_SAMPLE}
 *       windows), {@code MIXED_SOURCES}, {@code LOW_REPORTING_DENSITY} (a region in the bottom decile) and
 *       {@code LOW_DETECTABILITY} (own history used where 116 detectability is below {@code absenceMinDetect}). Every
 *       rate records a 116 manifest.
 *   <li><strong>Automatic rates</strong> ({@link #refreshAll}) cover every family × region with at least
 *       {@value #MIN_COVERAGE_WEEKS} weeks of coverage and at least one event, windows {@code P7D} and {@code P28D},
 *       replaced in place; region profiles are recomputed first. They run as 103 jobs of type
 *       {@value BaseRateRefreshRunner#TYPE}, monthly, after each finished import, or on request.
 *   <li><strong>Ad-hoc queries</strong> return a stored rate with the same definition, else compute one without
 *       storing it and cache it for {@value #CACHE_HOURS} hours.
 *   <li><strong>Egress:</strong> rates are derived statistics; the raw per-source counts of a {@code LOCAL_ONLY}
 *       source (ACLED) in {@code sourceMix} are shown only where {@link EgressPolicy#decide} allows that source on the
 *       path, and withheld otherwise.
 * </ul>
 */
@Service
public class BaseRateService implements ReferenceImportService.ImportListener {

    /** Fewer windows than this carry {@code SMALL_SAMPLE}. */
    public static final int SMALL_SAMPLE = 30;

    /** The coverage an automatic rate needs, in weeks. */
    public static final int MIN_COVERAGE_WEEKS = 52;

    /** The windows of automatic rates. */
    public static final List<String> AUTO_WINDOWS = List.of("P7D", "P28D");

    /** How long an ad-hoc result is cached. */
    public static final long CACHE_HOURS = 24;

    /** The most cached ad-hoc results. */
    public static final int CACHE_MAX = 2_000;

    /** The most regions checked for detectability per rate. */
    public static final int MAX_DETECTABILITY_CHECKS = 25;

    /** The weeks of history that reporting density averages over. */
    public static final int DENSITY_WEEKS = 520;

    /** The origins. */
    public static final List<String> ORIGINS = List.of("auto", "operator", "agent");

    private static final Logger log = LoggerFactory.getLogger(BaseRateService.class);

    /**
     * One computed rate.
     *
     * @param definition the definition
     * @param key the definition's key
     * @param result the calculator's result
     * @param licenses the contributing sources' licenses
     * @param unconditional the unconditional rate beside a conditional one, or null
     * @param caveats the caveats
     * @param computer the computer ({@code DetectorRef})
     * @param computedAt when it was computed
     * @param manifestId the 116 manifest, or null
     * @param profiles the region profiles of the reference class's regions
     */
    public record Computed(BaseRateDefinition definition, String key, BaseRateCalculator.Result result,
            List<Map<String, Object>> licenses, Map<String, Object> unconditional, List<Map<String, Object>> caveats,
            Map<String, Object> computer, Instant computedAt, String manifestId, List<RegionProfileEntity> profiles) {}

    /**
     * A page of rates.
     *
     * @param items the rates
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<BaseRateEntity> items, String nextCursor) {}

    private record Cached(Computed computed, Instant at) {}

    private final BaseRateRepository rates;
    private final RegionProfileRepository profiles;
    private final ReferenceImportRepository imports;
    private final RegionClasses regionClasses;
    private final BacktestJobService jobs;
    private final NamedParameterJdbcTemplate jdbc;
    private final AppSettingService settings;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<EgressPolicy> egress;
    private final Clock clock;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /** Creates the service. */
    public BaseRateService(BaseRateRepository rates, RegionProfileRepository profiles,
            ReferenceImportRepository imports, RegionClasses regionClasses, BacktestJobService jobs,
            NamedParameterJdbcTemplate jdbc, AppSettingService settings, ObjectProvider<ManifestService> manifests,
            ObjectProvider<ObservabilityService> observability, ObjectProvider<EgressPolicy> egress, Clock clock) {
        this.rates = rates;
        this.profiles = profiles;
        this.imports = imports;
        this.regionClasses = regionClasses;
        this.jobs = jobs;
        this.jdbc = jdbc;
        this.settings = settings;
        this.manifests = manifests;
        this.observability = observability;
        this.egress = egress;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- computation

    /** The key of a definition: the SHA-256 of its canonical JSON. */
    public static String key(BaseRateDefinition definition) {
        return CanonicalJson.sha256(canonical(definition.toMap()));
    }

    private static String canonical(Object value) {
        return String.valueOf(CanonicalJson.canonical(value));
    }

    /** The first week not to use: the Monday of the current, incomplete week. */
    LocalDate end() {
        return ReferenceFamilies.weekStart(LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC));
    }

    /** The regions of a definition: its codes, its class's countries, or every country with rows of the family. */
    List<String> regions(BaseRateDefinition definition) {
        return regions(definition, null);
    }

    /**
     * The regions of a definition as of a week: without codes or a class, every country with rows of the family in
     * the weeks before {@code before} (every week when null), so a country first seen later changes nothing.
     */
    List<String> regions(BaseRateDefinition definition, LocalDate before) {
        if (!definition.regionCodes().isEmpty()) {
            return definition.regionCodes();
        }
        if (definition.regionClass() != null) {
            return regionClasses.members(definition.regionClass());
        }
        MapSqlParameterSource p = new MapSqlParameterSource("family", definition.family());
        String bound = "";
        if (before != null) {
            bound = " AND week_start < :before";
            p.addValue("before", Date.valueOf(before));
        }
        return jdbc.queryForList("SELECT DISTINCT region_code FROM reference_history WHERE family = :family AND"
                + " length(region_code) = 2" + bound + " ORDER BY 1", p, String.class);
    }

    /** The weekly counts of a family in some regions (all regions when null). */
    BaseRateCalculator.Counts counts(String family, Collection<String> regions) {
        Map<String, Map<String, Map<LocalDate, Long>>> out = new HashMap<>();
        MapSqlParameterSource p = new MapSqlParameterSource("family", family);
        String where = "";
        if (regions != null) {
            if (regions.isEmpty()) {
                return new BaseRateCalculator.Counts(out);
            }
            where = " AND region_code IN (:regions)";
            p.addValue("regions", List.copyOf(regions));
        }
        jdbc.query("SELECT region_code, source, week_start, count FROM reference_history WHERE family = :family"
                + where, p, rs -> {
                    out.computeIfAbsent(rs.getString("region_code"), k -> new HashMap<>())
                            .computeIfAbsent(rs.getString("source"), k -> new HashMap<>())
                            .put(rs.getDate("week_start").toLocalDate(), rs.getLong("count"));
                });
        return new BaseRateCalculator.Counts(out);
    }

    /**
     * Every source's coverage: each import's completed chunks (a year for every region, a country-year for ACLED, a
     * file's period once done), and the own-history fold from its first row to the last folded week.
     */
    Map<String, BaseRateCalculator.Coverage> coverage() {
        Map<String, List<LocalDate[]>> global = new TreeMap<>();
        Map<String, Map<String, List<LocalDate[]>>> byCountry = new TreeMap<>();
        for (ReferenceImportEntity i : imports.findAll()) {
            String source = ReferenceFamilies.DATASET_SOURCE.get(i.getDataset());
            if (source == null) {
                continue;
            }
            Object done = i.getProgress().get("done");
            if (!(done instanceof Collection<?> chunks)) {
                continue;
            }
            for (Object o : chunks) {
                String chunk = String.valueOf(o);
                if (chunk.startsWith("file:")) {
                    if (i.getPeriodFrom() != null && i.getPeriodTo() != null) {
                        global.computeIfAbsent(source, k -> new ArrayList<>())
                                .add(new LocalDate[] {i.getPeriodFrom(), i.getPeriodTo()});
                    }
                } else if (chunk.matches("^[A-Z]{2}:[0-9]{4}$")) {
                    byCountry.computeIfAbsent(source, k -> new TreeMap<>())
                            .computeIfAbsent(chunk.substring(0, 2), k -> new ArrayList<>())
                            .add(ReferenceFamilies.yearChunk(Integer.parseInt(chunk.substring(3))));
                } else if (chunk.matches("^[0-9]{4}$")) {
                    global.computeIfAbsent(source, k -> new ArrayList<>())
                            .add(ReferenceFamilies.yearChunk(Integer.parseInt(chunk)));
                }
            }
        }
        LocalDate ownFirst = jdbc.query("SELECT min(week_start) FROM reference_history WHERE source = :source",
                new MapSqlParameterSource("source", ReferenceFamilies.FORESIGNAL),
                rs -> rs.next() && rs.getDate(1) != null ? rs.getDate(1).toLocalDate() : null);
        if (ownFirst != null) {
            LocalDate ownEnd = settings.get(SettingKeys.BASERATE_FOLD_WEEK, String.class).map(LocalDate::parse)
                    .orElse(end());
            if (ownFirst.isBefore(ownEnd)) {
                global.computeIfAbsent(ReferenceFamilies.FORESIGNAL, k -> new ArrayList<>())
                        .add(new LocalDate[] {ownFirst, ownEnd});
            }
        }
        Map<String, BaseRateCalculator.Coverage> out = new TreeMap<>();
        Set<String> sources = new TreeSet<>(global.keySet());
        sources.addAll(byCountry.keySet());
        for (String s : sources) {
            out.put(s, new BaseRateCalculator.Coverage(global.getOrDefault(s, List.of()),
                    byCountry.getOrDefault(s, Map.of())));
        }
        return out;
    }

    /**
     * Computes a definition without storing it.
     *
     * @param definition the definition
     * @return the computed rate
     */
    public Computed compute(BaseRateDefinition definition) {
        List<String> regions = regions(definition);
        Map<String, BaseRateCalculator.Coverage> coverage = coverage();
        BaseRateCalculator.Counts counts = counts(definition.family(), regions);
        BaseRateCalculator.Counts antecedent = definition.condition() == null ? null
                : counts(definition.condition().family(), regions);
        return compute(definition, regions, counts, antecedent, coverage, profileMap(), new HashMap<>(), end(),
                clock.instant());
    }

    /**
     * Computes a definition as of a past time, without storing or caching it (prompt 122, retrospective evaluation):
     * only the weeks before the ISO week of {@code asOf} are used, so later history changes nothing: the regions of a
     * definition without codes are those with earlier rows, coverage is cut off at that week (so a later import
     * cannot reorder the sources), and detectability is estimated at {@code asOf} (OBS-2).
     *
     * @param definition the definition
     * @param asOf the knowledge cutoff
     * @return the computed rate
     */
    public Computed computeAsOf(BaseRateDefinition definition, Instant asOf) {
        LocalDate end = ReferenceFamilies.weekStart(LocalDate.ofInstant(asOf, ZoneOffset.UTC));
        List<String> regions = regions(definition, end);
        Map<String, BaseRateCalculator.Coverage> coverage = new TreeMap<>();
        coverage().forEach((source, c) -> coverage.put(source, c.clippedAt(end)));
        BaseRateCalculator.Counts counts = counts(definition.family(), regions);
        BaseRateCalculator.Counts antecedent = definition.condition() == null ? null
                : counts(definition.condition().family(), regions);
        return compute(definition, regions, counts, antecedent, coverage, profileMap(), new HashMap<>(), end, asOf);
    }

    private Computed compute(BaseRateDefinition definition, List<String> regions, BaseRateCalculator.Counts counts,
            BaseRateCalculator.Counts antecedent, Map<String, BaseRateCalculator.Coverage> coverage,
            Map<String, RegionProfileEntity> profileMap, Map<String, Boolean> detectabilityCache, LocalDate end,
            Instant at) {
        BaseRateCalculator.Result result = BaseRateCalculator.compute(new BaseRateCalculator.Inputs(regions, counts,
                coverage, ReferenceFamilies.priority(definition.family()), definition.windowWeeks(), end, antecedent,
                definition.condition() == null ? null : ReferenceFamilies.priority(definition.condition().family()),
                definition.condition() == null ? 0 : definition.condition().withinDays()));
        if (result.periodFrom() == null) {
            result = new BaseRateCalculator.Result(result.ratePerWindow(), result.pAtLeastOne(), result.lower(),
                    result.upper(), result.nWindows(), result.nEvents(), end, end, result.sourceMix(),
                    result.sourcesUsed(), result.unconditional());
        }
        List<Map<String, Object>> licenses = new ArrayList<>();
        for (String source : result.sourcesUsed()) {
            Map<String, Object> l = new LinkedHashMap<>();
            l.put("source", source);
            l.put("licence", ReferenceFamilies.LICENSES.getOrDefault(source, "unknown"));
            l.put("egress", ReferenceFamilies.EGRESS.getOrDefault(source, "LOCAL_ONLY"));
            licenses.add(l);
        }
        List<RegionProfileEntity> regionProfiles = new ArrayList<>();
        for (String region : regions) {
            RegionProfileEntity p = profileMap.get(region.length() > 2 ? region.substring(0, 2) : region);
            if (p != null && regionProfiles.stream().noneMatch(x -> x.getRegionCode().equals(p.getRegionCode()))) {
                regionProfiles.add(p);
            }
        }
        List<Map<String, Object>> caveats = caveats(definition, result, regions, regionProfiles, detectabilityCache,
                at);
        Map<String, Object> unconditional = null;
        if (result.unconditional() != null) {
            unconditional = new LinkedHashMap<>();
            unconditional.put("ratePerWindow", result.unconditional().ratePerWindow());
            unconditional.put("pAtLeastOne", result.unconditional().pAtLeastOne());
            unconditional.put("interval", interval(result.unconditional().lower(), result.unconditional().upper()));
            unconditional.put("nWindows", result.unconditional().nWindows());
        }
        Map<String, Object> computer = new LinkedHashMap<>();
        computer.put("name", BaseRateCalculator.NAME);
        computer.put("version", BaseRateCalculator.VERSION);
        computer.put("params", Map.of("smallSample", SMALL_SAMPLE, "intervalLevel", BaseRateCalculator.LEVEL,
                "intervalMethod", "jeffreys"));
        Instant now = clock.instant();
        String key = key(definition);
        return new Computed(definition, key, result, licenses, unconditional, caveats, computer, now,
                manifest(definition, result, end), regionProfiles);
    }

    private List<Map<String, Object>> caveats(BaseRateDefinition definition, BaseRateCalculator.Result result,
            List<String> regions, List<RegionProfileEntity> regionProfiles, Map<String, Boolean> detectabilityCache,
            Instant at) {
        List<String> undetectable = result.sourcesUsed().contains(ReferenceFamilies.FORESIGNAL)
                ? lowDetectability(ReferenceFamilies.root(definition.family()), regions, detectabilityCache, at)
                : List.of();
        return BaseRateCaveats.of(definition, result, regionProfiles, undetectable, minDetect());
    }

    private double minDetect() {
        ObservabilityService o = observability.getIfAvailable();
        return o == null ? ObservabilityService.DEFAULT_ABSENCE_MIN_DETECT : o.absenceMinDetect();
    }

    private List<String> lowDetectability(String family, List<String> regions, Map<String, Boolean> cache,
            Instant now) {
        ObservabilityService o = observability.getIfAvailable();
        List<String> out = new ArrayList<>();
        if (o == null) {
            return out;
        }
        double min = o.absenceMinDetect();
        int checked = 0;
        for (String region : regions) {
            if (checked++ >= MAX_DETECTABILITY_CHECKS) {
                break;
            }
            Boolean lowHere = cache.computeIfAbsent(family + "|" + region, k -> {
                try {
                    return o.pDetect(family, Place.region(region), now, null).p() < min;
                } catch (RuntimeException e) {
                    return true;
                }
            });
            if (Boolean.TRUE.equals(lowHere)) {
                out.add(region);
            }
        }
        return out;
    }

    private String manifest(BaseRateDefinition definition, BaseRateCalculator.Result result, LocalDate end) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("table", "reference_history");
        query.put("end", end.toString());
        query.put("sources", List.copyOf(result.sourcesUsed()));
        query.put("periodFrom", result.periodFrom() == null ? null : result.periodFrom().toString());
        query.put("periodTo", result.periodTo() == null ? null : result.periodTo().toString());
        try {
            return service.record(ManifestRequest.of(BaseRateCalculator.NAME, BaseRateCalculator.VERSION)
                    .config(definition.toMap()).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of a base rate: {}", e.toString());
            return null;
        }
    }

    /** The {@code StatInterval} of a Jeffreys interval. */
    static Map<String, Object> interval(double lower, double upper) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lower", lower);
        out.put("upper", upper);
        out.put("level", BaseRateCalculator.LEVEL);
        out.put("method", "jeffreys");
        return out;
    }

    private Map<String, RegionProfileEntity> profileMap() {
        Map<String, RegionProfileEntity> out = new HashMap<>();
        for (RegionProfileEntity p : profiles.findAll()) {
            out.put(p.getRegionCode(), p);
        }
        return out;
    }

    // ------------------------------------------------------------------------- API

    /**
     * Lists stored rates, ordered by id (keyset).
     *
     * @param families families (any), or empty
     * @param regions regions (overlap), or empty
     * @param window a window, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     * @return the page
     */
    @Transactional(readOnly = true)
    public Page list(List<String> families, List<String> regions, String window, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id FROM base_rate WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        if (families != null && !families.isEmpty()) {
            sql.append(" AND family IN (:families)");
            p.addValue("families", families);
        }
        if (regions != null && !regions.isEmpty()) {
            sql.append(" AND region_codes && CAST(:regions AS text[])");
            p.addValue("regions", ai.foresignal.store.query.PgArrays.text(regions));
        }
        if (window != null) {
            sql.append(" AND window_code = :window");
            p.addValue("window", window);
        }
        String after = decode(cursor);
        if (after != null) {
            sql.append(" AND id > :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY id LIMIT :limit");
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        List<BaseRateEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            rates.findById(ids.get(i)).ifPresent(items::add);
        }
        return new Page(items, ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId())
                : null);
    }

    /**
     * One stored rate.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_BASE_RATE}
     */
    @Transactional(readOnly = true)
    public BaseRateEntity get(String id) {
        return (id == null ? java.util.Optional.<BaseRateEntity>empty() : rates.findById(id)).orElseThrow(
                () -> new NotFoundException(ErrorCodes.UNKNOWN_BASE_RATE, "No base rate '" + id + "'."));
    }

    /**
     * The result of an ad-hoc query.
     *
     * @param stored the stored rate with the same definition, or null
     * @param computed the computed (or cached) rate when none is stored, or null
     * @param cached true when {@code computed} came from the cache
     */
    public record QueryResult(BaseRateEntity stored, Computed computed, boolean cached) {}

    /**
     * An ad-hoc query: the stored rate with this definition (the newest), else a computed one, cached for
     * {@value #CACHE_HOURS} hours.
     *
     * @throws ValidationException 400 {@code BASE_RATE_INVALID}
     */
    public QueryResult query(Map<String, Object> definitionMap) {
        BaseRateDefinition definition = BaseRateDefinition.parse(definitionMap);
        String key = key(definition);
        List<BaseRateEntity> stored = new ArrayList<>(rates.findByDefinitionKey(key));
        if (!stored.isEmpty()) {
            stored.sort((a, b) -> b.getComputedAt().compareTo(a.getComputedAt()));
            return new QueryResult(stored.get(0), null, false);
        }
        Instant now = clock.instant();
        Cached hit = cache.get(key);
        if (hit != null && hit.at().plus(Duration.ofHours(CACHE_HOURS)).isAfter(now)) {
            return new QueryResult(null, hit.computed(), true);
        }
        Computed computed = compute(definition);
        if (cache.size() >= CACHE_MAX) {
            cache.entrySet().removeIf(e -> e.getValue().at().plus(Duration.ofHours(CACHE_HOURS)).isBefore(now));
            if (cache.size() >= CACHE_MAX) {
                cache.clear();
            }
        }
        cache.put(key, new Cached(computed, now));
        return new QueryResult(null, computed, false);
    }

    /** Empties the ad-hoc cache (tests, and after a refresh). */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Computes and stores a custom definition ({@code BaseRateInput}).
     *
     * @param name the name, or null
     * @param definitionMap the definition
     * @param origin {@code operator} or {@code agent}
     * @param actor who stored it
     * @return the stored rate
     * @throws ValidationException 400 {@code BASE_RATE_INVALID}
     */
    @Transactional
    public BaseRateEntity create(String name, Map<String, Object> definitionMap, String origin, String actor) {
        if (name != null && name.length() > 200) {
            throw new ValidationException(ErrorCodes.BASE_RATE_INVALID, "The name is longer than 200 characters.",
                    List.of(new ai.foresignal.api.error.FieldError("name", "must be at most 200 characters")));
        }
        BaseRateDefinition definition = BaseRateDefinition.parse(definitionMap);
        Computed computed = compute(definition);
        BaseRateEntity entity = new BaseRateEntity(Ulids.next(clock.instant()), "operator".equals(origin) ? "operator"
                : "agent");
        entity.setName(name);
        entity.setDefinition(definition, computed.key());
        entity.setCreatedBy(actor);
        entity.apply(computed);
        return rates.save(entity);
    }

    /**
     * Deletes a custom rate.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_BASE_RATE}
     * @throws ConflictException 409 {@code BASE_RATE_NOT_CUSTOM} for an automatic rate
     */
    @Transactional
    public void delete(String id) {
        BaseRateEntity entity = get(id);
        if ("auto".equals(entity.getOrigin())) {
            throw new ConflictException(ErrorCodes.BASE_RATE_NOT_CUSTOM, "Base rate " + id + " is automatic; it is"
                    + " replaced at every refresh and cannot be deleted.");
        }
        rates.delete(entity);
    }

    /** The region profiles of the given regions (by their country). */
    public List<RegionProfileEntity> profilesOf(Collection<String> regions) {
        Map<String, RegionProfileEntity> map = profileMap();
        Set<String> seen = new LinkedHashSet<>();
        List<RegionProfileEntity> out = new ArrayList<>();
        for (String r : regions) {
            String country = r.length() > 2 ? r.substring(0, 2) : r;
            RegionProfileEntity p = map.get(country);
            if (p != null && seen.add(country)) {
                out.add(p);
            }
        }
        return out;
    }

    /**
     * Whether the raw counts of {@code source} may appear on {@code path}: OPEN sources always; a {@code LOCAL_ONLY}
     * source as {@link EgressPolicy#decide} says for its registered source id.
     */
    public boolean mixAllowed(String source, EgressPath path) {
        if (!"LOCAL_ONLY".equals(ReferenceFamilies.EGRESS.get(source))) {
            return true;
        }
        EgressPolicy policy = egress.getIfAvailable();
        String sourceId = ReferenceFamilies.SOURCE_IDS.get(source);
        if (policy == null || sourceId == null) {
            return false;
        }
        return policy.decide(path, sourceId, null, null, EgressPolicy.EgressContext.NONE).allowed();
    }

    // ------------------------------------------------------------------------- refresh

    /**
     * Starts a refresh of the automatic rates and region profiles as a 103 job.
     *
     * @param trigger {@code manual}, {@code schedule}, {@code import} or {@code cycle}
     * @param tokenId the submitting token, or null
     * @return the queued job
     * @throws ConflictException 409 {@code BACKTEST_IN_PROGRESS} while a refresh is queued or running
     */
    @Transactional
    public BacktestJobEntity submitRefresh(String trigger, String tokenId) {
        Long inFlight = jdbc.queryForObject("SELECT count(*) FROM backtest_job WHERE type = :type AND status IN"
                + " ('queued', 'running')", new MapSqlParameterSource("type", BaseRateRefreshRunner.TYPE), Long.class);
        if (inFlight != null && inFlight > 0) {
            throw new ConflictException(ErrorCodes.BACKTEST_IN_PROGRESS, "A base-rate refresh is already queued or"
                    + " running.");
        }
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("trigger", trigger == null ? "manual" : trigger);
        return jobs.submitRunnerJob(BaseRateRefreshRunner.TYPE, null, definition, tokenId);
    }

    @Override
    public void finished(ReferenceImportEntity entity) {
        try {
            submitRefresh("import", null);
        } catch (ConflictException e) {
            log.debug("Base-rate refresh after import {} not queued: {}", entity.getId(), e.getMessage());
        }
    }

    /**
     * Recomputes the region profiles.
     *
     * @return the profiles written
     */
    @Transactional
    public int refreshProfiles() {
        LocalDate end = end();
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", Date.valueOf(end.minusWeeks(
                DENSITY_WEEKS))).addValue("to", Date.valueOf(end));
        Map<String, Map<String, Long>> totals = new TreeMap<>();
        jdbc.query("SELECT region_code, source, sum(count) AS n FROM reference_history WHERE (family = 'conflict' OR"
                + " family LIKE 'conflict:%') AND length(region_code) = 2 AND region_code <> 'ZZ' AND week_start >="
                + " :from AND week_start < :to GROUP BY 1, 2", p, rs -> {
                    totals.computeIfAbsent(rs.getString("region_code"), k -> new TreeMap<>())
                            .put(rs.getString("source"), rs.getLong("n"));
                });
        Map<String, ReportingDensity.Input> inputs = new TreeMap<>();
        Map<String, RegionClasses.Country> countries = regionClasses.countries();
        Set<String> codes = new TreeSet<>(countries.keySet());
        codes.addAll(totals.keySet());
        for (String code : codes) {
            long max = totals.getOrDefault(code, Map.of()).values().stream().mapToLong(Long::longValue).max()
                    .orElse(0);
            RegionClasses.Country c = countries.get(code);
            inputs.put(code, new ReportingDensity.Input((double) max / DENSITY_WEEKS, c == null ? null
                    : c.population()));
        }
        Map<String, ReportingDensity.Profile> computed = ReportingDensity.compute(inputs);
        Instant now = clock.instant();
        // An upsert per region, then the removal of regions no longer computed: two passes running at once (the
        // refresh job and a direct call, or two jobs) both converge on the same rows instead of one inserting a
        // region the other just inserted (a delete-all-then-insert races on the primary key), and readers never
        // see an empty table between the delete and the insert.
        java.sql.Timestamp at = java.sql.Timestamp.from(now);
        MapSqlParameterSource[] rows = computed.entrySet().stream()
                .map(e -> new MapSqlParameterSource().addValue("code", e.getKey())
                        .addValue("density", e.getValue().density()).addValue("decile", e.getValue().decile())
                        .addValue("at", at))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate("INSERT INTO region_profile (region_code, reporting_density, density_decile, computed_at)"
                + " VALUES (:code, :density, :decile, :at) ON CONFLICT (region_code) DO UPDATE SET"
                + " reporting_density = EXCLUDED.reporting_density, density_decile = EXCLUDED.density_decile,"
                + " computed_at = EXCLUDED.computed_at", rows);
        if (computed.isEmpty()) {
            jdbc.update("DELETE FROM region_profile", new MapSqlParameterSource());
        } else {
            jdbc.update("DELETE FROM region_profile WHERE region_code NOT IN (:codes)",
                    new MapSqlParameterSource("codes", computed.keySet()));
        }
        return rows.length;
    }

    /**
     * Recomputes every automatic rate (the job's work): region profiles first, then every family × region with at
     * least {@value #MIN_COVERAGE_WEEKS} covered weeks and one event, windows {@code P7D} and {@code P28D}; automatic
     * rates not recomputed are removed at the end.
     *
     * @param monitor the job monitor
     * @return the {@code BaseRateRefreshResult}
     */
    public Map<String, Object> refreshAll(JobMonitor monitor) {
        Instant started = clock.instant();
        int profileCount = refreshProfiles();
        Map<String, BaseRateCalculator.Coverage> coverage = coverage();
        Map<String, RegionProfileEntity> profileMap = profileMap();
        List<String> families = jdbc.queryForList("SELECT DISTINCT family FROM reference_history ORDER BY 1",
                new MapSqlParameterSource(), String.class);
        Map<String, Boolean> detectability = new HashMap<>();
        LocalDate end = end();
        int written = 0;
        int skipped = 0;
        Set<String> regionsSeen = new TreeSet<>();
        for (int f = 0; f < families.size(); f++) {
            monitor.checkpoint((double) f / Math.max(1, families.size()));
            String family = families.get(f);
            BaseRateCalculator.Counts counts = counts(family, null);
            for (String region : new TreeSet<>(counts.counts().keySet())) {
                if ("ZZ".equals(region)) {
                    continue;
                }
                List<String> regions = List.of(region);
                boolean enough = true;
                for (String window : AUTO_WINDOWS) {
                    BaseRateDefinition definition = new BaseRateDefinition(family, regions, null, window, null);
                    Computed computed = compute(definition, regions, counts, null, coverage, profileMap,
                            detectability, end, clock.instant());
                    if ("P7D".equals(window) && computed.result().nWindows() < MIN_COVERAGE_WEEKS) {
                        enough = false;
                    }
                    if (!enough || countsFor(counts, region) == 0) {
                        skipped++;
                        break;
                    }
                    upsertAuto(definition, computed);
                    written++;
                    regionsSeen.add(region);
                }
            }
        }
        int removed = jdbc.update("DELETE FROM base_rate WHERE origin = 'auto' AND computed_at < :started",
                new MapSqlParameterSource("started", java.sql.Timestamp.from(started)));
        clearCache();
        monitor.checkpoint(1);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profiles", profileCount);
        out.put("rates", written);
        out.put("skipped", skipped);
        out.put("removed", removed);
        out.put("families", families);
        out.put("regions", regionsSeen.size());
        out.put("computer", Map.of("name", BaseRateCalculator.NAME, "version", BaseRateCalculator.VERSION));
        log.info("Base rates refreshed: {} rates over {} families and {} regions, {} profiles, {} removed", written,
                families.size(), regionsSeen.size(), profileCount, removed);
        return out;
    }

    private static long countsFor(BaseRateCalculator.Counts counts, String region) {
        long total = 0;
        for (Map<LocalDate, Long> weeks : counts.counts().getOrDefault(region, Map.of()).values()) {
            for (Long n : weeks.values()) {
                total += n;
            }
        }
        return total;
    }

    /** Stores an automatic rate, replacing the one with the same definition. */
    @Transactional
    public BaseRateEntity upsertAuto(BaseRateDefinition definition, Computed computed) {
        List<BaseRateEntity> existing = rates.findByDefinitionKeyAndOrigin(computed.key(), "auto");
        BaseRateEntity entity = existing.isEmpty() ? new BaseRateEntity(Ulids.next(clock.instant()), "auto")
                : existing.get(0);
        entity.setDefinition(definition, computed.key());
        entity.apply(computed);
        return rates.save(entity);
    }

    private static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String id = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (Ulids.isUlid(id)) {
                return id;
            }
        } catch (IllegalArgumentException e) {
            // fall through
        }
        throw ValidationException.forField("cursor", "is not a cursor of this list");
    }
}

package ai.foresignal.reference;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.reference.ReferenceManifest.DatasetSpec;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jakarta.annotation.PreDestroy;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Loads the reference datasets of the bundled {@link ReferenceManifest} (prompt 040).
 *
 * <ol>
 *   <li><b>Fetch.</b> Downloads through the budgeted {@link UpstreamClient} ({@link #SYSTEM_ID}, a small
 *       fixed budget, Foresignal's identifiable User-Agent) into {@code ${FORESIGNAL_DATA_DIR}/reference/};
 *       or, with {@code foresignal.reference.offline-dir}, reads the pre-placed file of the same name. The
 *       URLs come only from the manifest.
 *   <li><b>Verify.</b> The file's SHA-256 must equal the pin ({@code foresignal.reference.checksums} wins
 *       over the manifest). A mismatch is {@code REFERENCE_CHECKSUM_MISMATCH}: the file is deleted and the
 *       dataset is not loaded. GeoNames regenerates its dumps daily, so its rolling entries are verified
 *       only when the operator pins a checksum; otherwise the observed hash is recorded.
 *   <li><b>Stage.</b> GeoJSON is streamed ({@link GeoJsonFeatures}, Foresignal's GeoJSON reader,
 *       {@code GeometryFixer}); GeoNames TSV is parsed line by line. Rows go into a staging table.
 *   <li><b>Swap.</b> One transaction runs {@code TRUNCATE} and {@code INSERT … SELECT} from the staging
 *       table, merging features that share a key; {@code geom_simple} is
 *       {@code ST_SimplifyPreserveTopology(geom, 0.01)}. Readers see the old rows or the new ones,
 *       never a mixture.
 * </ol>
 *
 * <p>Loads run one at a time on a background thread. On the first start, when
 * {@code foresignal.reference.auto-load} is on and nothing is loaded yet, the default datasets load in the
 * background; this never blocks startup or readiness. Failures are recorded in {@code ref_dataset}, shown
 * in the {@code reference} health detail and announced as a {@code system.reference_load_failed} event.
 */
@Component
public class ReferenceDataLoader {

    /** The id the loader's downloads are budgeted, metered and backed off under. */
    public static final String SYSTEM_ID = "foresignal-reference";

    /** Dataset states. */
    public static final String ABSENT = "absent";
    /** Dataset states. */
    public static final String LOADING = "loading";
    /** Dataset states. */
    public static final String LOADED = "loaded";
    /** Dataset states. */
    public static final String FAILED = "failed";

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataLoader.class);
    private static final Pattern ISO_3166_2 = Pattern.compile("^[A-Z]{2}-[A-Z0-9]{1,3}$");
    private static final Pattern FIPS5 = Pattern.compile("^[0-9]{5}$");
    private static final int POLYGON_BATCH = 100;
    private static final int POINT_BATCH = 2_000;

    /** Staging tables, created on first use; dropped rows are truncated before every load. */
    static final Map<String, String> STAGING = Map.of(
            "ref_stage_country", "CREATE TABLE IF NOT EXISTS ref_stage_country (iso2 varchar(2), iso3 varchar(3),"
                    + " name varchar(200), geom geometry(Geometry,4326))",
            "ref_stage_admin1", "CREATE TABLE IF NOT EXISTS ref_stage_admin1 (code varchar(32), iso2 varchar(2),"
                    + " name varchar(200), gn_code varchar(32), geom geometry(Geometry,4326))",
            "ref_stage_admin2", "CREATE TABLE IF NOT EXISTS ref_stage_admin2 (fips5 varchar(5), state_code varchar(8),"
                    + " name varchar(200), geom geometry(Geometry,4326))",
            "ref_stage_sea", "CREATE TABLE IF NOT EXISTS ref_stage_sea (id bigint, name varchar(200),"
                    + " geom geometry(Geometry,4326))",
            "ref_stage_place", "CREATE TABLE IF NOT EXISTS ref_stage_place (geonameid bigint, name varchar(200),"
                    + " ascii_name varchar(200), iso2 varchar(2), admin1_code varchar(20), feature_code varchar(10),"
                    + " population bigint, timezone varchar(40), lon double precision, lat double precision)",
            "ref_stage_gn_admin1", "CREATE TABLE IF NOT EXISTS ref_stage_gn_admin1 (code varchar(32),"
                    + " name varchar(200), ascii_name varchar(200), geonameid bigint)",
            "ref_stage_gn_country", "CREATE TABLE IF NOT EXISTS ref_stage_gn_country (iso2 varchar(2),"
                    + " iso3 varchar(3), iso_numeric varchar(3), fips varchar(2), name varchar(200),"
                    + " capital varchar(200), population bigint, continent varchar(2), geonameid bigint)");

    private static final String MULTI = "ST_Multi(ST_CollectionExtract(ST_MakeValid(%s), 3))";

    /** The swap of each kind: the target table, then the statements run after its TRUNCATE. */
    static final Map<String, List<String>> SWAP = Map.of(
            ReferenceManifest.KIND_NE_ADMIN0, List.of("ref_country",
                    "INSERT INTO ref_country (iso2, iso3, name, geom) SELECT iso2, max(iso3),"
                            + " (array_agg(name ORDER BY ST_Area(geom) DESC))[1], " + MULTI.formatted("ST_Union(geom)")
                            + " FROM ref_stage_country GROUP BY iso2",
                    "UPDATE ref_country SET geom_simple = "
                            + MULTI.formatted("ST_SimplifyPreserveTopology(geom, 0.01)")),
            ReferenceManifest.KIND_NE_ADMIN1, List.of("ref_admin1",
                    "INSERT INTO ref_admin1 (code, iso2, name, gn_code, geom) SELECT code, max(iso2),"
                            + " (array_agg(name ORDER BY ST_Area(geom) DESC))[1], max(gn_code), "
                            + MULTI.formatted("ST_Union(geom)") + " FROM ref_stage_admin1 GROUP BY code",
                    "UPDATE ref_admin1 SET geom_simple = "
                            + MULTI.formatted("ST_SimplifyPreserveTopology(geom, 0.01)")),
            ReferenceManifest.KIND_NE_ADMIN2, List.of("ref_admin2",
                    "INSERT INTO ref_admin2 (fips5, state_code, name, geom) SELECT fips5, max(state_code),"
                            + " (array_agg(name ORDER BY ST_Area(geom) DESC))[1], " + MULTI.formatted("ST_Union(geom)")
                            + " FROM ref_stage_admin2 GROUP BY fips5"),
            ReferenceManifest.KIND_NE_MARINE, List.of("ref_sea",
                    "INSERT INTO ref_sea (id, name, geom) SELECT id, max(name), " + MULTI.formatted("ST_Union(geom)")
                            + " FROM ref_stage_sea GROUP BY id"),
            ReferenceManifest.KIND_GN_CITIES, List.of("gazetteer_place",
                    "INSERT INTO gazetteer_place (geonameid, name, ascii_name, iso2, admin1_code, feature_code,"
                            + " population, timezone, geog) SELECT DISTINCT ON (geonameid) geonameid, name, ascii_name,"
                            + " iso2, admin1_code, feature_code, population, timezone,"
                            + " ST_SetSRID(ST_MakePoint(lon, lat), 4326)::geography FROM ref_stage_place"
                            + " ORDER BY geonameid"),
            ReferenceManifest.KIND_GN_ADMIN1, List.of("gazetteer_admin1",
                    "INSERT INTO gazetteer_admin1 (code, name, ascii_name, geonameid) SELECT DISTINCT ON (code) code,"
                            + " name, ascii_name, geonameid FROM ref_stage_gn_admin1 ORDER BY code"),
            ReferenceManifest.KIND_GN_COUNTRYINFO, List.of("gazetteer_country",
                    "INSERT INTO gazetteer_country (iso2, iso3, iso_numeric, fips, name, capital, population,"
                            + " continent, geonameid) SELECT DISTINCT ON (iso2) iso2, iso3, iso_numeric, fips, name,"
                            + " capital, population, continent, geonameid FROM ref_stage_gn_country ORDER BY iso2"));

    private final ReferenceManifest manifest;
    private final ReferenceProperties properties;
    private final ForesignalProperties foresignal;
    private final UpstreamClient upstream;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(Thread.ofVirtual().name("reference-loader").factory());
    private final Map<String, String> inFlight = new ConcurrentHashMap<>();
    private final AtomicInteger queued = new AtomicInteger();
    private volatile String lastError;

    /** Creates the loader. */
    public ReferenceDataLoader(ReferenceManifest manifest, ReferenceProperties properties,
            ForesignalProperties foresignal, UpstreamClient upstream, JdbcTemplate jdbc,
            PlatformTransactionManager transactionManager, ApplicationEventPublisher events, Clock clock) {
        this.manifest = manifest;
        this.properties = properties;
        this.foresignal = foresignal;
        this.upstream = upstream;
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.events = events;
        this.clock = clock;
    }

    /** Stops the background thread. */
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    // ------------------------------------------------------------------------- first start

    /**
     * Schedules the default datasets on the first start ({@code auto-load}, nothing loaded yet), after
     * {@code auto-load-delay}, on the loader's own thread. Never blocks startup or readiness.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.REFERENCE)
    public void onReady() {
        if (!properties.autoLoad()) {
            return;
        }
        Long loaded;
        try {
            loaded = jdbc.queryForObject("SELECT count(*) FROM ref_dataset WHERE state = ?", Long.class, LOADED);
        } catch (RuntimeException e) {
            log.warn("Could not read the reference data state; the first-start load is skipped: {}", e.getMessage());
            return;
        }
        if (loaded != null && loaded > 0) {
            return;
        }
        List<String> names = properties.datasets().isEmpty() ? manifest.defaults() : properties.datasets();
        List<DatasetSpec> specs = specs(names);
        queued.incrementAndGet();
        executor.submit(() -> {
            try {
                Thread.sleep(properties.autoLoadDelay());
                log.info("Loading the reference datasets {} in the background (first start)", names);
                loadNow(specs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                queued.decrementAndGet();
            }
        });
    }

    // ------------------------------------------------------------------------- API

    /**
     * Queues {@code names} for loading on the background thread ({@code loadReference}, 202).
     *
     * @throws ValidationException for an empty list or an unknown name
     */
    public void loadAsync(List<String> names) {
        List<DatasetSpec> specs = specs(names);
        for (DatasetSpec spec : specs) {
            inFlight.put(spec.name(), LOADING);
        }
        queued.incrementAndGet();
        executor.submit(() -> {
            try {
                loadNow(specs);
            } finally {
                queued.decrementAndGet();
            }
        });
    }

    /**
     * The manifest entries of {@code names}, with the configured point-of-view variant applied to
     * admin-0.
     *
     * @throws ValidationException for an empty list or an unknown name
     */
    public List<DatasetSpec> specs(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw ValidationException.forField("datasets", "name at least one dataset");
        }
        List<DatasetSpec> out = new ArrayList<>();
        for (String name : names) {
            DatasetSpec spec = manifest.find(name).orElseThrow(() -> ValidationException.forField("datasets",
                    "unknown dataset '" + name + "'; known: " + manifest.datasets().stream().map(DatasetSpec::name)
                            .toList()));
            if (out.stream().noneMatch(s -> s.name().equals(spec.name()))) {
                out.add(variant(spec));
            }
        }
        return out;
    }

    private DatasetSpec variant(DatasetSpec spec) {
        String key = properties.neAdmin0Variant();
        if (key == null || !ReferenceManifest.KIND_NE_ADMIN0.equals(spec.kind())) {
            return spec;
        }
        ReferenceManifest.Variant v = spec.variants().get(key.toLowerCase(Locale.ROOT));
        if (v == null) {
            log.warn("Unknown Natural Earth point-of-view variant '{}'; loading the default admin-0 file", key);
            return spec;
        }
        return spec.with(v.url(), v.sha256());
    }

    /**
     * Loads {@code specs} now, one after another, on the calling thread (the background thread, and
     * tests). A failing dataset does not stop the others.
     *
     * @return the state of each dataset afterwards ({@link #LOADED} or {@link #FAILED})
     */
    public synchronized Map<String, String> loadNow(List<DatasetSpec> specs) {
        Map<String, String> results = new LinkedHashMap<>();
        List<String> changed = new ArrayList<>();
        for (DatasetSpec spec : specs) {
            inFlight.put(spec.name(), LOADING);
            markLoading(spec);
            try {
                long rows = load(spec);
                results.put(spec.name(), LOADED);
                changed.add(spec.name());
                log.info("Loaded reference dataset {} ({} rows)", spec.name(), rows);
            } catch (ReferenceDataException e) {
                fail(spec, e.code(), e.getMessage());
                results.put(spec.name(), FAILED);
            } catch (RuntimeException e) {
                fail(spec, ReferenceDataException.LOAD_FAILED, e.getMessage());
                results.put(spec.name(), FAILED);
            } finally {
                inFlight.remove(spec.name());
            }
        }
        if (!changed.isEmpty()) {
            events.publishEvent(new ReferenceDataChangedEvent(changed));
            // Geofence presets resolve against the new boundaries: the engine and the persist gate recompile.
            events.publishEvent(new ai.foresignal.trigger.TriggerRulesChangedEvent(null));
        }
        return results;
    }

    /** True while a load is queued or running. */
    public boolean loading() {
        return queued.get() > 0 || !inFlight.isEmpty();
    }

    /** The last failure message, or null. */
    public String lastError() {
        return lastError;
    }

    /** The {@code ReferenceStatus} document. */
    public Map<String, Object> status() {
        Map<String, Map<String, Object>> rows = new LinkedHashMap<>();
        jdbc.query("SELECT name, edition, url, sha256, licence, attribution, rows, loaded_at, state, error"
                + " FROM ref_dataset", rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("edition", rs.getString("edition"));
                    row.put("url", rs.getString("url"));
                    row.put("sha256", rs.getString("sha256"));
                    row.put("licence", rs.getString("licence"));
                    row.put("attribution", rs.getString("attribution"));
                    long count = rs.getLong("rows");
                    row.put("rows", rs.wasNull() ? null : count);
                    Timestamp at = rs.getTimestamp("loaded_at");
                    row.put("loadedAt", at == null ? null : at.toInstant().toString());
                    row.put("state", rs.getString("state"));
                    row.put("error", rs.getString("error"));
                    rows.put(rs.getString("name"), row);
                });
        List<Map<String, Object>> datasets = new ArrayList<>();
        for (DatasetSpec spec : manifest.datasets()) {
            Map<String, Object> stored = rows.getOrDefault(spec.name(), Map.of());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", spec.name());
            item.put("edition", stored.getOrDefault("edition", spec.edition()));
            item.put("url", stored.get("url") != null ? stored.get("url") : spec.url());
            item.put("sha256", stored.get("sha256") != null ? stored.get("sha256") : spec.sha256());
            item.put("licence", spec.licence());
            item.put("attribution", spec.attribution());
            item.put("rows", stored.get("rows"));
            item.put("loadedAt", stored.get("loadedAt"));
            item.put("optional", spec.optional());
            String state = inFlight.containsKey(spec.name()) ? LOADING
                    : (String) stored.getOrDefault("state", ABSENT);
            item.put("state", state == null ? ABSENT : state);
            item.put("error", stored.get("error"));
            datasets.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("datasets", datasets);
        out.put("loading", loading());
        out.put("attributions", RegionAttribution.all());
        out.put("boundaryNote", RegionAttribution.BOUNDARY_NOTE);
        return out;
    }

    // ------------------------------------------------------------------------- one dataset

    private long load(DatasetSpec spec) {
        Path file = fetch(spec);
        String expected = properties.checksums().getOrDefault(spec.name(), spec.rolling() ? null : spec.sha256());
        String actual = sha256(file);
        if (expected != null && !expected.equalsIgnoreCase(actual)) {
            if (properties.offlineDir() == null) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    log.warn("Could not delete {} after a checksum mismatch", file);
                }
            }
            throw new ReferenceDataException(ReferenceDataException.CHECKSUM_MISMATCH, spec.fileName()
                    + " has SHA-256 " + actual + ", expected " + expected + "; the dataset was not loaded");
        }
        if (expected == null) {
            log.info("Reference dataset {} is rolling and has no pinned checksum; recording SHA-256 {}", spec.name(),
                    actual);
        }
        long rows = stageAndSwap(spec, file);
        record(spec, actual, rows);
        return rows;
    }

    /** The dataset's file: the pre-placed one from {@code offlineDir}, or a fresh download. */
    Path fetch(DatasetSpec spec) {
        if (properties.offlineDir() != null) {
            Path placed = properties.offlineDir().resolve(spec.fileName());
            if (!Files.isRegularFile(placed)) {
                throw new ReferenceDataException(ReferenceDataException.FILE_MISSING,
                        spec.fileName() + " is not in the offline directory " + properties.offlineDir());
            }
            return placed;
        }
        Path dir = foresignal.dataDir().resolve("reference");
        Path target = dir.resolve(spec.fileName());
        Path part = dir.resolve(spec.fileName() + ".part");
        try {
            Files.createDirectories(dir);
            DownloadResult result = http().download(URI.create(spec.url()), part, spec.maxBytes());
            Files.move(result.file(), target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return target;
        } catch (AdapterException e) {
            throw new ReferenceDataException(ReferenceDataException.DOWNLOAD_FAILED,
                    "could not download " + spec.fileName() + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ReferenceDataException(ReferenceDataException.DOWNLOAD_FAILED,
                    "could not store " + spec.fileName() + ": " + e.getMessage(), e);
        }
    }

    private SourceHttp http() {
        SourceDescriptor descriptor = SourceDescriptor.builder(SYSTEM_ID)
                .name("Reference data")
                .description("Natural Earth and GeoNames reference datasets (prompt 040)")
                .mode(Mode.POLLING)
                .build();
        return upstream.forSystem(descriptor, Budget.of(properties.requestsPerMinute(), properties.requestsPerDay()));
    }

    /** Parses {@code file} into the dataset's staging table and swaps it in; the rows of the target. */
    long stageAndSwap(DatasetSpec spec, Path file) {
        List<String> swap = SWAP.get(spec.kind());
        if (swap == null) {
            throw new ReferenceDataException(ReferenceDataException.LOAD_FAILED, "unknown dataset kind " + spec.kind());
        }
        String stage = switch (spec.kind()) {
            case ReferenceManifest.KIND_NE_ADMIN0 -> stageCountries(file);
            case ReferenceManifest.KIND_NE_ADMIN1 -> stageAdmin1(file);
            case ReferenceManifest.KIND_NE_ADMIN2 -> stageAdmin2(file);
            case ReferenceManifest.KIND_NE_MARINE -> stageSeas(file);
            case ReferenceManifest.KIND_GN_CITIES -> stagePlaces(file);
            case ReferenceManifest.KIND_GN_ADMIN1 -> stageGeoNamesAdmin1(file);
            case ReferenceManifest.KIND_GN_COUNTRYINFO -> stageCountryInfo(file);
            default -> throw new ReferenceDataException(ReferenceDataException.LOAD_FAILED,
                    "unknown dataset kind " + spec.kind());
        };
        String target = swap.getFirst();
        Long rows = transactions.execute(status -> {
            jdbc.execute("TRUNCATE " + target);
            for (String sql : swap.subList(1, swap.size())) {
                jdbc.execute(sql);
            }
            return jdbc.queryForObject("SELECT count(*) FROM " + target, Long.class);
        });
        jdbc.execute("TRUNCATE " + stage);
        return rows == null ? 0 : rows;
    }

    private String prepare(String stage) {
        jdbc.execute(STAGING.get(stage));
        jdbc.execute("TRUNCATE " + stage);
        return stage;
    }

    private String stageCountries(Path file) {
        String stage = prepare("ref_stage_country");
        polygons(file, stage, "INSERT INTO ref_stage_country (iso2, iso3, name, geom) VALUES (?, ?, ?,"
                + " ST_GeomFromWKB(?, 4326))", (p, g) -> {
                    String iso2 = code(p, "ISO_A2_EH");
                    if (iso2 == null || iso2.length() != 2) {
                        return null;
                    }
                    String iso3 = code(p, "ISO_A3_EH");
                    return new Object[] {iso2, iso3 != null && iso3.length() == 3 ? iso3 : null,
                        text(p, "NAME", iso2), g};
                });
        return stage;
    }

    private String stageAdmin1(Path file) {
        String stage = prepare("ref_stage_admin1");
        polygons(file, stage, "INSERT INTO ref_stage_admin1 (code, iso2, name, gn_code, geom) VALUES (?, ?, ?, ?,"
                + " ST_GeomFromWKB(?, 4326))", (p, g) -> {
                    String iso = code(p, "iso_3166_2");
                    String code = iso != null && ISO_3166_2.matcher(iso).matches() ? iso : code(p, "adm1_code");
                    if (code == null) {
                        return null;
                    }
                    String iso2 = code(p, "iso_a2");
                    return new Object[] {code, iso2 != null && iso2.length() == 2 ? iso2 : null,
                        text(p, "name", code), code(p, "gn_a1_code"), g};
                });
        return stage;
    }

    private String stageAdmin2(Path file) {
        String stage = prepare("ref_stage_admin2");
        polygons(file, stage, "INSERT INTO ref_stage_admin2 (fips5, state_code, name, geom) VALUES (?, ?, ?,"
                + " ST_GeomFromWKB(?, 4326))", (p, g) -> {
                    if (!"US".equals(code(p, "ISO_A2"))) {
                        return null;
                    }
                    String fips = code(p, "CODE_LOCAL");
                    if (fips == null || !FIPS5.matcher(fips).matches()) {
                        String full = code(p, "FIPS");
                        fips = full != null && full.length() == 7 ? full.substring(2) : null;
                    }
                    if (fips == null || !FIPS5.matcher(fips).matches()) {
                        return null;
                    }
                    String region = code(p, "REGION");
                    return new Object[] {fips, region == null ? null : "US-" + region, text(p, "NAME", fips), g};
                });
        return stage;
    }

    private String stageSeas(Path file) {
        String stage = prepare("ref_stage_sea");
        AtomicInteger synthetic = new AtomicInteger();
        polygons(file, stage, "INSERT INTO ref_stage_sea (id, name, geom) VALUES (?, ?, ST_GeomFromWKB(?, 4326))",
                (p, g) -> {
                    String name = text(p, "name", null);
                    if (name == null) {
                        return null;
                    }
                    long id = p.path("ne_id").isNumber() ? p.path("ne_id").asLong() : -synthetic.incrementAndGet();
                    return new Object[] {id, name, g};
                });
        return stage;
    }

    private interface Row {
        Object[] of(JsonNode properties, byte[] wkb);
    }

    private void polygons(Path file, String stage, String insert, Row row) {
        List<Object[]> batch = new ArrayList<>();
        WKBWriter writer = new WKBWriter(2);
        try (InputStream in = Files.newInputStream(file)) {
            GeoJsonFeatures.forEachPolygonal(in, (properties, geometry) -> {
                Object[] values = row.of(properties, writer.write(geometry));
                if (values != null) {
                    batch.add(values);
                    if (batch.size() >= POLYGON_BATCH) {
                        jdbc.batchUpdate(insert, batch);
                        batch.clear();
                    }
                }
            });
        } catch (IOException | IllegalArgumentException e) {
            throw new ReferenceDataException(ReferenceDataException.LOAD_FAILED,
                    "could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(insert, batch);
        }
    }

    private String stagePlaces(Path file) {
        String stage = prepare("ref_stage_place");
        String insert = "INSERT INTO ref_stage_place (geonameid, name, ascii_name, iso2, admin1_code, feature_code,"
                + " population, timezone, lon, lat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        List<Object[]> batch = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".txt")) {
                    continue;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(zip, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] c = line.split("\t", -1);
                    if (c.length < 18 || c[0].isBlank()) {
                        continue;
                    }
                    try {
                        double lat = Double.parseDouble(c[4]);
                        double lon = Double.parseDouble(c[5]);
                        batch.add(new Object[] {Long.parseLong(c[0]), cut(c[1], 200), cut(blank(c[2]), 200),
                            cut(blank(c[8]), 2), cut(blank(c[10]), 20), cut(blank(c[7]), 10),
                            c[14].isBlank() ? null : Long.parseLong(c[14]), cut(blank(c[17]), 40), lon, lat});
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (batch.size() >= POINT_BATCH) {
                        jdbc.batchUpdate(insert, batch);
                        batch.clear();
                    }
                }
                break;
            }
        } catch (IOException e) {
            throw new ReferenceDataException(ReferenceDataException.LOAD_FAILED,
                    "could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(insert, batch);
        }
        return stage;
    }

    private String stageGeoNamesAdmin1(Path file) {
        String stage = prepare("ref_stage_gn_admin1");
        String insert = "INSERT INTO ref_stage_gn_admin1 (code, name, ascii_name, geonameid) VALUES (?, ?, ?, ?)";
        tsv(file, insert, c -> c.length < 4 || c[0].isBlank() ? null
                : new Object[] {cut(c[0], 32), cut(c[1], 200), cut(blank(c[2]), 200), longOrNull(c[3])});
        return stage;
    }

    private String stageCountryInfo(Path file) {
        String stage = prepare("ref_stage_gn_country");
        String insert = "INSERT INTO ref_stage_gn_country (iso2, iso3, iso_numeric, fips, name, capital, population,"
                + " continent, geonameid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        tsv(file, insert, c -> c.length < 17 || c[0].length() != 2 ? null
                : new Object[] {c[0], cut(blank(c[1]), 3), cut(blank(c[2]), 3), cut(blank(c[3]), 2), cut(c[4], 200),
                    cut(blank(c[5]), 200), longOrNull(c[7]), cut(blank(c[8]), 2), longOrNull(c[16])});
        return stage;
    }

    private interface TsvRow {
        Object[] of(String[] cells);
    }

    private void tsv(Path file, String insert, TsvRow row) {
        List<Object[]> batch = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                Object[] values = row.of(line.split("\t", -1));
                if (values != null) {
                    batch.add(values);
                }
                if (batch.size() >= POINT_BATCH) {
                    jdbc.batchUpdate(insert, batch);
                    batch.clear();
                }
            }
        } catch (IOException e) {
            throw new ReferenceDataException(ReferenceDataException.LOAD_FAILED,
                    "could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(insert, batch);
        }
    }

    // ------------------------------------------------------------------------- ref_dataset

    private void markLoading(DatasetSpec spec) {
        jdbc.update("INSERT INTO ref_dataset (name, edition, url, licence, attribution, state) VALUES (?, ?, ?, ?, ?, ?)"
                + " ON CONFLICT (name) DO UPDATE SET state = EXCLUDED.state", spec.name(), spec.edition(), spec.url(),
                spec.licence(), spec.attribution(), LOADING);
    }

    private void record(DatasetSpec spec, String sha256, long rows) {
        jdbc.update("UPDATE ref_dataset SET edition = ?, url = ?, sha256 = ?, licence = ?, attribution = ?, rows = ?,"
                + " loaded_at = ?, state = ?, error = NULL WHERE name = ?", spec.edition(), spec.url(), sha256,
                spec.licence(), spec.attribution(), rows, Timestamp.from(clock.instant()), LOADED, spec.name());
    }

    private void fail(DatasetSpec spec, String code, String message) {
        lastError = spec.name() + ": " + code + (message == null ? "" : " (" + message + ")");
        log.warn("Could not load reference dataset {}: {} {}", spec.name(), code, message);
        try {
            // A dataset loaded earlier stays in its table (state loaded); its row records the failure.
            jdbc.update("UPDATE ref_dataset SET state = CASE WHEN loaded_at IS NULL THEN ? ELSE ? END, error = ?"
                    + " WHERE name = ?", FAILED, LOADED, code, spec.name());
        } catch (RuntimeException e) {
            log.warn("Could not record the failure of reference dataset {}", spec.name(), e);
        }
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("dataset", spec.name());
            data.put("code", code);
            events.publishEvent(new SystemEvent("system.reference_load_failed", "warning",
                    "Reference dataset " + spec.name() + " was not loaded",
                    "Loading " + spec.name() + " failed with " + code + ". Region tags fall back to the bundled"
                            + " country table until it loads.", "/v1/reference", data));
        } catch (RuntimeException e) {
            log.warn("Could not publish the reference failure notification", e);
        }
    }

    /** True when at least one row of {@code table} exists. */
    public boolean hasRows(String table) {
        try {
            Boolean any = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM " + table + ")", Boolean.class);
            return Boolean.TRUE.equals(any);
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------- helpers

    /** The SHA-256 of {@code file}, hex. */
    public static String sha256(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1 << 16];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            throw new ReferenceDataException(ReferenceDataException.LOAD_FAILED,
                    "could not read " + file.getFileName() + ": " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String code(JsonNode properties, String field) {
        JsonNode node = properties.path(field);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() || value.startsWith("-99") ? null : value;
    }

    private static String text(JsonNode properties, String field, String fallback) {
        String value = code(properties, field);
        return value == null ? fallback : cut(value, 200);
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String cut(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private static Long longOrNull(String value) {
        try {
            return value == null || value.isBlank() ? null : Long.parseLong(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The dataset row of {@code name}, when one is stored (tests). */
    Optional<String> state(String name) {
        List<String> states = jdbc.queryForList("SELECT state FROM ref_dataset WHERE name = ?", String.class, name);
        return states.isEmpty() ? Optional.empty() : Optional.ofNullable(states.getFirst());
    }

    /** Converts a polygonal geometry to WKB (tests). */
    static byte[] wkb(MultiPolygon geometry) {
        return new WKBWriter(2).write(geometry);
    }
}

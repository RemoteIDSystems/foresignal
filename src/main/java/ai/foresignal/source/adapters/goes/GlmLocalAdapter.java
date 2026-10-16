package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;

/**
 * {@code glm-local-{station}} (prompt 107): GLM L2 LCFA files written by a SatDump GOES GRB pipeline
 * (1686.6 MHz; a larger dish than HRIT), decoded with the 048 {@link GlmParser} and fed into the shared
 * AOI flash ring tagged {@code origin: local}, de-duplicated against the internet {@code goes-glm}
 * flashes by (time ± 1 s, position ± 0.05°).
 *
 * <p>The clusterer runs here only while {@code goes-glm} is unavailable (failing, disabled or idle), so
 * {@code lightning-cluster} records keep flowing from local flashes when the internet feed is down, and
 * the two never mint clusters at the same time. When {@code goes-glm} recovers, every open local cluster
 * is ended (tombstoned) on the next run.
 *
 * <p>Files are read in modification-time order behind a watermark. A file is read only once it has
 * been unmodified for {@value #SETTLE_SECONDS} s; a young file that fails to parse stops the run and is
 * retried, so a partially written file is never skipped past.
 *
 * <p>Health: the newest input is recorded only while the station's {@code ground-station} state is
 * {@code locked} or {@code receiving}, so {@link ai.foresignal.collect.InputAgeHealthRule} degrades when
 * no file arrives for {@value #MAX_GAP_MINUTES} minutes during a pass and the source stays idle otherwise.
 */
public class GlmLocalAdapter implements PollingAdapter {

    /** The source id prefix. */
    public static final String PREFIX = "glm-local-";

    /** The {@code inboxPath} parameter. */
    public static final String INBOX_PATH = "inboxPath";

    /** The most files decoded per run. */
    static final int MAX_FILES_PER_RUN = 4;

    /** The largest file read. */
    static final int MAX_FILE_BYTES = 8 * 1024 * 1024;

    /** The gap after which health degrades during a pass. */
    static final long MAX_GAP_MINUTES = 5;

    /** The flash origin tag, and the archive directory local flashes are written to. */
    public static final String ORIGIN = "local";

    /** The state keys. */
    static final String LAST_MTIME = "lastMtime";
    static final String CLUSTERS = "clusters";

    private final String station;
    private final String sourceId;
    private final NetcdfDecoder decoder;
    private final RasterPaths paths;
    private final CurrentRecords records;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    public GlmLocalAdapter(String station, NetcdfDecoder decoder, RasterPaths paths, CurrentRecords records) {
        this.station = station;
        this.sourceId = PREFIX + station;
        this.decoder = decoder;
        this.paths = paths;
        this.records = records;
        this.descriptor = buildDescriptor(sourceId, station);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The station name. */
    public String station() {
        return station;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return FetchResult.of(List.of());
        }
        Path dir = ctx.params().inboxPath(INBOX_PATH);
        if (dir == null) {
            return FetchResult.of(List.of());
        }
        List<InboxAccess.FileStat> files = new ArrayList<>();
        try {
            for (Path file : ctx.inbox().listRegularFiles(dir)) {
                if (file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".nc")) {
                    files.add(ctx.inbox().stat(file));
                }
            }
        } catch (IOException e) {
            throw new AdapterException(sourceId + ": cannot list " + dir + ": " + e.getMessage(), e);
        }
        files.sort(Comparator.comparing(InboxAccess.FileStat::modified));
        Instant now = ctx.now();
        Instant lastMtime = ctx.state().get(LAST_MTIME).map(Instant::parse).orElse(null);
        FlashSink flashes = ctx.rasters().flashes();
        LightningClusterer clusterer = new LightningClusterer();
        clusterer.restore(ctx.state().get(CLUSTERS).orElse(null));
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        Instant newest = lastMtime;
        int processed = 0;
        boolean internetDown = internetUnavailable();
        for (InboxAccess.FileStat file : files) {
            if (processed >= MAX_FILES_PER_RUN) {
                break;
            }
            if (lastMtime != null && !file.modified().isAfter(lastMtime)) {
                continue;
            }
            if (!settled(file, now)) {
                // Possibly still being written: stop here so the watermark does not pass it.
                break;
            }
            if (file.size() > MAX_FILE_BYTES) {
                ctx.log().warn("{}: skipping {} ({} bytes over the cap)", sourceId, file.path().getFileName(),
                        file.size());
                newest = file.modified();
                continue;
            }
            Path staged = stage(ctx, file);
            if (staged == null) {
                break;
            }
            GlmParser.Result parsed;
            try {
                parsed = GlmParser.parse(decoder, staged, sourceId);
            } catch (Exception e) {
                GzipInputs.deleteQuietly(staged);
                if (retryable(file, now)) {
                    // A young file that does not parse is most likely still being written: retry next run.
                    ctx.log().debug("{}: {} not readable yet: {}", sourceId, file.path().getFileName(), e.getMessage());
                    break;
                }
                ctx.log().warn("{}: {} skipped: {}", sourceId, file.path().getFileName(), e.getMessage());
                newest = file.modified();
                continue;
            }
            try {
                // The store de-duplicates against the internet copy in both directions (prompt 107).
                flashes.append(ORIGIN, ORIGIN, parsed.flashes(), parsed.productTime());
                if (internetDown) {
                    long deltaMs = lastMtime == null ? 0 : Duration.between(lastMtime, file.modified()).toMillis();
                    FlashBatch window = flashes.between(parsed.productTime().minus(LightningClusterer.WINDOW),
                            parsed.productTime());
                    collect(out, clusterer.step(window, parsed.productTime().toEpochMilli(), deltaMs));
                }
                newest = file.modified();
                processed++;
                ctx.log().debug("{}: {} flashes appended (local)", sourceId, parsed.total());
            } finally {
                GzipInputs.deleteQuietly(staged);
            }
        }
        if (!internetDown && !clusterer.tracked().isEmpty()) {
            // goes-glm is back and mints the clusters again: end every local cluster now, so none is left open.
            collect(out, clusterer.step(FlashBatch.EMPTY, now.toEpochMilli(), LightningClusterer.QUIET_MS));
        }
        clusterer.markEmitted(out.values().stream().filter(r -> !r.tombstone())
                .map(NormalizedRecord::externalId).toList());
        if (newest != null && !newest.equals(lastMtime)) {
            ctx.state().put(LAST_MTIME, newest.toString());
        }
        ctx.state().put(CLUSTERS, clusterer.serialize());
        if (newest != null && stationState()) {
            ctx.state().recordInput(newest, Duration.ofMinutes(MAX_GAP_MINUTES));
        } else {
            ctx.state().remove(ai.foresignal.source.spi.AdapterStateStore.NEWEST_INPUT_KEY);
            ctx.state().remove(ai.foresignal.source.spi.AdapterStateStore.MAX_INPUT_GAP_KEY);
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /** A file is settled once its modification time is at least {@value #SETTLE_SECONDS} s old. */
    static final long SETTLE_SECONDS = 10;

    /** A file that fails to parse is retried while it is younger than this, seconds. */
    static final long RETRY_SECONDS = 120;

    /** True when {@code file} has not been modified for {@value #SETTLE_SECONDS} s, so it is complete. */
    static boolean settled(InboxAccess.FileStat file, Instant now) {
        return !file.modified().isAfter(now.minusSeconds(SETTLE_SECONDS));
    }

    /** True when a parse failure of {@code file} should be retried (it is younger than {@value #RETRY_SECONDS} s). */
    static boolean retryable(InboxAccess.FileStat file, Instant now) {
        return file.modified().isAfter(now.minusSeconds(RETRY_SECONDS));
    }

    /** Folds clusterer changes into {@code out}: a tombstone of a never-emitted cluster cancels its record. */
    private static void collect(Map<String, NormalizedRecord> out, List<LightningClusterer.Change> changes) {
        for (LightningClusterer.Change change : changes) {
            NormalizedRecord record = change.record();
            if (record.tombstone() && !change.wasEmitted()) {
                out.remove(record.externalId());
            } else {
                out.put(record.externalId(), record);
            }
        }
    }

    private Path stage(FetchContext ctx, InboxAccess.FileStat file) {
        try (java.io.InputStream in = ctx.inbox().open(file.path())) {
            Path staged = Files.createTempFile(paths.tmp(), sourceId + "-", ".nc");
            java.nio.file.Files.copy(in, staged, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return staged;
        } catch (IOException | RuntimeException e) {
            ctx.log().warn("{}: {} could not be staged: {}", sourceId, file.path().getFileName(), e.getMessage());
            return null;
        }
    }

    /** True while the internet {@code goes-glm} source is unavailable. */
    private boolean internetUnavailable() {
        return !records.available("goes-glm");
    }

    /** True while the station's {@code ground-station} state is locked or receiving. */
    private boolean stationState() {
        for (CurrentRecords.Row row : records.current(List.of("satdump-" + station), List.of("ground-station"), 1)) {
            Object state = row.payload().get("state");
            String s = state == null ? "" : state.toString().toLowerCase(Locale.ROOT);
            return "locked".equals(s) || "receiving".equals(s);
        }
        return false;
    }

    private static SourceDescriptor buildDescriptor(String sourceId, String station) {
        return SourceDescriptor.builder(sourceId)
                .name("Local GLM (GRB) station " + station)
                .description("GLM L2 LCFA lightning from SatDump's GOES GRB reception at station '" + station
                        + "', fed into the shared AOI ring tagged origin:local and de-duplicated against the"
                        + " internet goes-glm flashes. Clusters keep flowing when the internet feed is down. Foresignal"
                        + " reads SatDump's files only.")
                .kind(LightningClusterer.KIND)
                .param(ParamSpec.required(INBOX_PATH, ParamType.INBOX_PATH,
                        "The directory under FORESIGNAL_INBOX_DIR where SatDump writes GOES GRB GLM L2 LCFA NetCDF"
                                + " files, for example satdump/" + station + "/glm."))
                .homepage("https://github.com/SatDump/SatDump")
                .attribution("NOAA GOES-R GLM via the operator's SatDump GRB station")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-goes/")
                .mode(Mode.POLLING)
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(30))
                .defaultBudget(Budget.of(60, 50_000))
                .recordTtl(null)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .historyTier(HistoryTier.STANDARD)
                .zeroResultsNormal(true)
                .defaultRetention(Duration.ofDays(3))
                .kindRetention(LightningClusterer.KIND, Duration.ofDays(3))
                .build();
    }
}

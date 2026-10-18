package ai.foresignal.chronicle;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Samples the {@code rollup:} and {@code rule:} refs of active chronicles' series layers (prompt 131), and the
 * {@code social:} refs of {@code social-volume} layers (prompt 137): those series are
 * computed (activity rollups, anomaly rule samples), not records, so the capture loop cannot copy their versions.
 * Every {@code foresignal.chronicles.series-sample-interval} (15 min by default) each such ref is read through the 119
 * {@link SeriesReader} from the last captured period (at most a day before the chronicle started) to now, and each
 * point is upserted into {@code chronicle_series_point} with its {@code knownAt}; a revised bucket replaces the stored
 * one. {@code series:} refs are captured from their {@code series-point} records instead.
 */
@Component
public class ChronicleSeriesSampler {

    private static final Logger log = LoggerFactory.getLogger(ChronicleSeriesSampler.class);

    private final ObjectProvider<SeriesReader> reader;
    private final ChronicleStorage storage;
    private final JdbcTemplate jdbc;
    private final ChronicleProperties properties;
    private final Clock clock;
    private final Map<String, Instant> lastSampled = new ConcurrentHashMap<>();

    /** Creates the sampler. */
    public ChronicleSeriesSampler(ObjectProvider<SeriesReader> reader, ChronicleStorage storage, JdbcTemplate jdbc,
            ChronicleProperties properties, Clock clock) {
        this.reader = reader;
        this.storage = storage;
        this.jdbc = jdbc;
        this.properties = properties;
        this.clock = clock;
    }

    /** Samples every computed series ref whose interval elapsed. */
    public int sampleDue(ChronicleIndex index) {
        return sample(index, false);
    }

    /**
     * Samples the computed series refs of every active chronicle.
     *
     * @param index the active chronicles
     * @param force true to ignore the interval (tests and the activation of a chronicle)
     * @return the points written or revised
     */
    public int sample(ChronicleIndex index, boolean force) {
        int written = 0;
        Instant now = clock.instant();
        for (ChronicleIndex.Entry e : index.entries()) {
            for (CompiledLayer layer : e.layers()) {
                List<String> refs;
                if (layer.selector() instanceof ChronicleSelector.Series s) {
                    refs = s.refs();
                } else if (layer.selector() instanceof ChronicleSelector.SocialVolume v) {
                    refs = v.refs();
                } else {
                    continue;
                }
                for (String text : refs) {
                    String key = layer.layerId() + '\u0001' + text;
                    Instant last = lastSampled.get(key);
                    if (!force && last != null && last.plus(properties.seriesSampleInterval()).isAfter(now)) {
                        continue;
                    }
                    lastSampled.put(key, now);
                    written += sampleRef(e.chronicle(), layer, text, now);
                }
            }
        }
        return written;
    }

    private int sampleRef(ChronicleEntity chronicle, CompiledLayer layer, String text, Instant now) {
        SeriesReader series = reader.getIfAvailable();
        if (series == null) {
            return 0;
        }
        SeriesRef ref;
        try {
            ref = SeriesRef.parse(text);
        } catch (RuntimeException e) {
            return 0;
        }
        if (ref.type() == SeriesRef.Type.SERIES) {
            return 0;
        }
        Instant started = chronicle.getStartedAt() == null ? now : chronicle.getStartedAt();
        Instant from = lastPeriod(chronicle.getId(), text);
        if (from == null) {
            from = started.minus(Duration.ofDays(1));
        }
        if (!from.isBefore(now)) {
            return 0;
        }
        int written = 0;
        try {
            SeriesData data = series.read(ref, from, now, null);
            long fresh = 0;
            for (SeriesValue v : data.points()) {
                if (storage.upsertSeriesPoint(chronicle.getId(), new ChronicleStorage.SeriesPointWrite(layer.layerId(),
                        text, v.period(), v.periodStart(), v.periodEnd(), v.value(), data.unit(), v.status(),
                        v.version(), v.knownAt(), v.coverageOk(), null, false), now)) {
                    fresh++;
                }
                written++;
            }
            storage.advance(chronicle.getId(), null, now, fresh, fresh * ChronicleSizes.SERIES_ROW);
            storage.layerCounters(layer.layerId(), fresh, fresh * ChronicleSizes.SERIES_ROW, null);
        } catch (RuntimeException e) {
            log.debug("Could not sample {} for chronicle {}: {}", text, chronicle.getId(), e.toString());
        }
        return written;
    }

    private Instant lastPeriod(String chronicleId, String ref) {
        List<java.sql.Timestamp> rows = jdbc.queryForList("SELECT max(period_start) FROM chronicle_series_point"
                + " WHERE chronicle_id = ? AND ref = ?", java.sql.Timestamp.class, chronicleId, ref);
        return rows.isEmpty() || rows.getFirst() == null ? null : rows.getFirst().toInstant();
    }

    /** An instant from a payload value (an RFC 3339 string), else {@code fallback}. */
    static Instant instant(Object value, Instant fallback) {
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Instant.parse(s);
            } catch (DateTimeParseException e) {
                return fallback;
            }
        }
        return fallback;
    }
}

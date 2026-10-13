package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * One {@code transit-alerts-{id}} (or {@code transit-alerts-mdb-{id}}) source (prompt 070): a GTFS-RT ServiceAlerts
 * feed as {@code transit-alert} records ({@link TransitDescriptors#alerts}, {@link GtfsRtAlertParser}).
 *
 * <p><strong>Absence.</strong> GTFS-RT {@code FULL_DATASET} semantics mean an alert missing from the next full feed
 * has ended. When the header says FULL_DATASET, the ids of a successful, fully parsed response are compared with the
 * previous ones ({@link LiveIdSet}, scope {@value #LIVE_SCOPE}, adapter state {@code live:alerts}) and every missing
 * id gets an explicit tombstone; an entity marked {@code is_deleted} is tombstoned too. A 304 changes nothing, and a
 * failed, truncated or unparseable response throws before any comparison, so an outage never reads as "every alert
 * ended". A {@code DIFFERENTIAL} feed tombstones only on {@code is_deleted}. Tombstones carry the header timestamp
 * (the fetch time when the header has none).
 */
public class TransitAlertsAdapter implements PollingAdapter {

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "alerts";

    /** The {@link LiveIdSet} scope of the alert ids of the last full response. */
    public static final String LIVE_SCOPE = "alerts";

    /** An accepted {@code language} parameter: a BCP 47 tag. */
    static final Pattern LANGUAGE = Pattern.compile("^[A-Za-z]{2,8}(-[A-Za-z0-9]{1,8})*$");

    private final TransitFeed feed;
    private final SourceDescriptor descriptor;
    private final Supplier<String> installId;

    /**
     * Creates the source.
     *
     * @param feed the feed entry; its {@code alertsUrl} must be set
     * @param installId supplies the install id for the feed's headers
     */
    public TransitAlertsAdapter(TransitFeed feed, Supplier<String> installId) {
        if (feed.alertsUrl() == null) {
            throw new IllegalArgumentException(feed.id() + " has no alerts URL");
        }
        this.feed = feed;
        this.descriptor = TransitDescriptors.alerts(feed);
        this.installId = installId == null ? () -> null : installId;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The feed entry. */
    public TransitFeed feed() {
        return feed;
    }

    /** The request headers, with the install id filled in. */
    public Map<String, String> requestHeaders() {
        return feed.resolvedHeaders(installId.get());
    }

    /** {@code language} must be a BCP 47 tag. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String language = params.optionalString(TransitDescriptors.LANGUAGE_PARAM).orElse(null);
        if (language != null && !LANGUAGE.matcher(language.strip()).matches()) {
            problems.put(TransitDescriptors.LANGUAGE_PARAM, "must be a BCP 47 language tag such as en, fi or nb-NO");
        }
        return problems;
    }

    /**
     * Fetches the feed once.
     *
     * @return {@link FetchResult#unchanged()} on a 304; otherwise the alerts and their tombstones
     * @throws AdapterException when the request fails or the body is not a GTFS-RT FeedMessage
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        FeedMessage message = GtfsRt.fetch(ctx, feed.alertsUrl(), STATE_KEY, requestHeaders());
        if (message == null) {
            return FetchResult.unchanged();
        }
        String language = ctx.params().optionalString(TransitDescriptors.LANGUAGE_PARAM)
                .orElse(GtfsRtAlertParser.DEFAULT_LANGUAGE);
        GtfsRtAlertParser.Result result = GtfsRtAlertParser.parse(message, language);
        Instant tombstoneAt = result.observedAt() != null ? result.observedAt() : ctx.now();
        List<NormalizedRecord> out = new ArrayList<>(result.records());
        Set<String> gone = new LinkedHashSet<>(result.deleted());
        if (result.fullDataset()) {
            Set<String> live = new LinkedHashSet<>();
            result.records().forEach(r -> live.add(r.externalId()));
            gone.addAll(LiveIdSet.absentSince(ctx.state(), LIVE_SCOPE, live));
            gone.removeAll(live);
        }
        for (String id : gone) {
            out.add(NormalizedRecord.tombstone(id, GtfsRtAlertParser.KIND, tombstoneAt));
        }
        ctx.log().debug("{}: {} alert(s), {} tombstone(s)", ctx.sourceId(), result.records().size(), gone.size());
        return result.fullDataset() ? FetchResult.complete(out) : FetchResult.of(out);
    }
}

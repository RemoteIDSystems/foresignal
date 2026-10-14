package ai.foresignal.source.adapters.uasiren;

import ai.foresignal.reference.ukr.UaRegionIndex;
import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ua-air-raid-siren} (prompt 081): Ukrainian alerts from the keyless siren.pp.ua wrapper of
 * {@code api.ukrainealarm.com} ({@link UaSirenParser}).
 *
 * <p>Each run is a conditional GET of {@value #ALERTS_URL} (the upstream sends an {@code ETag}; state key
 * {@value #STATE_KEY}); a 304 is {@link FetchResult#unchanged()}. An alert that disappears from two consecutive
 * complete fetches is tombstoned ({@link AbsenceTombstoner}, per kind). Shipped disabled: the wrapper is run by an
 * individual, publishes no terms, and {@code alerts-in-ua} is the preferred keyed source.
 */
@Component
public class UaSirenAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ua-air-raid-siren";

    /** The alerts in force. */
    public static final String ALERTS_URL = "https://siren.pp.ua/api/v3/alerts";

    /** The conditional GET state key. */
    public static final String STATE_KEY = "siren-alerts";

    /** The description. */
    static final String DESCRIPTION = "Air-raid, artillery and urban-fight alerts in force in Ukraine from"
            + " siren.pp.ua, a keyless public wrapper of api.ukrainealarm.com run by an individual"
            + " (github.com/PaulAnnekov/uasiren; 50 requests per minute per IP, no license or terms published). One"
            + " record per region and alert type, with the oblast or raion polygon (a community alert carries its"
            + " raion's polygon and geometryApproximate). Some regions show alerts unchanged since 2022 (occupied"
            + " areas): that is normal, not stale data. alerts-in-ua is the preferred, keyed source; the official"
            + " api.ukrainealarm.com needs an application key.";

    private final URI alerts;
    private final UaRegionIndex regions;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value ConflictSources#BASE_URL_PROPERTY}; blank in production
     * @param regions the Ukraine region index
     */
    @Autowired
    public UaSirenAdapter(@Value("${" + ConflictSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            UaRegionIndex regions) {
        this.alerts = ConflictSources.uri(ALERTS_URL, baseUrlOverride);
        this.regions = regions;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The alerts URL. */
    public URI alertsUri() {
        return alerts;
    }

    /**
     * Reads the alerts conditionally and maps them with the tombstones of the ones gone.
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the body does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(alerts, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        UaSirenParser.Result parsed;
        try {
            parsed = UaSirenParser.parse(ConflictSources.body(ctx, response, STATE_KEY, alerts), regions);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        Instant now = ctx.now();
        List<NormalizedRecord> records = new ArrayList<>(parsed.records());
        for (Map.Entry<String, List<String>> scope : parsed.activeIds().entrySet()) {
            List<String> gone = AbsenceTombstoner.update(ctx.state(), scope.getKey(),
                    AbsenceTombstoner.Outcome.COMPLETE, scope.getValue());
            records.addAll(AbsenceTombstoner.tombstones(gone, scope.getKey(), now));
        }
        if (!parsed.skipped().isEmpty()) {
            ctx.log().debug("{}: skipped {}", ID, parsed.skipped());
        }
        return FetchResult.complete(records);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Ukraine air-raid sirens (siren.pp.ua)")
                .description(DESCRIPTION)
                .kinds(UaSirenParser.AIR_RAID_KIND, UaSirenParser.THREAT_KIND)
                .homepage("https://siren.pp.ua")
                .attribution("siren.pp.ua (data from api.ukrainealarm.com)")
                .licence("unspecified (siren.pp.ua wrapper of ukrainealarm.com)")
                .termsUrl("https://github.com/PaulAnnekov/uasiren")
                .mode(Mode.POLLING)
                .interval(Duration.ofSeconds(15), Duration.ofSeconds(30))
                .defaultBudget(Budget.of(4, 4000))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(ConflictSources.EVENT_RETENTION)
                .kindRetention(UaSirenParser.AIR_RAID_KIND, ConflictSources.EVENT_RETENTION)
                .kindRetention(UaSirenParser.THREAT_KIND, ConflictSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .egress(Egress.OPEN)
                .titleField("region")
                .tileProperties("alertType", "alertLevel", "region")
                .searchFields("region", "regionEn");
        ConflictSources.text(b, "region", "Region", "The region's name (Ukrainian)");
        ConflictSources.text(b, "regionEn", "Region (English)", "The region's English name, where given");
        ConflictSources.text(b, "regionType", "Region type", "State (oblast), District (raion) or Community");
        ConflictSources.text(b, "alertType", "Alert type", "AIR, ARTILLERY or URBAN_FIGHTS");
        ConflictSources.text(b, "alertLevel", "Alert level", "The highest active level: red or yellow");
        ConflictSources.bool(b, "geometryApproximate", "Approximate area", "True when the polygon is the raion that"
                + " contains the alert's community");
        b.kindDisplay(UaSirenParser.AIR_RAID_KIND, new KindDisplay("{region}: air raid", "warning", "#C92A2A", null));
        b.kindDisplay(UaSirenParser.THREAT_KIND, new KindDisplay("{region}: {alertType}", "danger", "#E8590C", null));
        return b.build();
    }
}

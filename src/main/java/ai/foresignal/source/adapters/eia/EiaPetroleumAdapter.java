package ai.foresignal.source.adapters.eia;

import ai.foresignal.source.series.SeriesPeriods;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.JsonProps;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code eia-petroleum} (prompt 093): EIA daily spot prices and weekly crude stocks as {@code series-point} records.
 *
 * <p>Spot series come from {@code petroleum/pri/spt} (daily; the WPSR weekly release refreshes them) and stocks from
 * {@code petroleum/stoc/wstk} (weekly, Wednesdays). Each poll re-reads the trailing 60 rows, so a revision becomes a
 * new version and an unchanged point a throttled touch. The key is sent as {@code api_key} in the URL (EIA's only
 * option) and is masked by the upstream client.
 *
 * <p>The spot series are sourced by EIA from Refinitiv (LSEG), so the source is {@code LOCAL_ONLY}: its raw values
 * are served only to this instance's own clients.
 */
@Component
public class EiaPetroleumAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = EiaApi.PETROLEUM_ID;

    /** The spot-series parameter. */
    public static final String SPOT_PARAM = "spotSeries";

    /** The stock-series parameter. */
    public static final String STOCK_PARAM = "stockSeries";

    /** The default spot series (WTI Cushing and Brent). */
    public static final List<String> DEFAULT_SPOT = List.of("RWTC", "RBRTE");

    /** The default weekly stock series. */
    public static final List<String> DEFAULT_STOCK = List.of("WCESTUS1");

    /** Rows re-read per series each poll. */
    static final int TRAILING_ROWS = 60;

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public EiaPetroleumAdapter(@Value("${" + EiaApi.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? EiaApi.DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(EiaApi.API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + EiaApi.API_KEY + " is not set (a key from"
                    + " eia.gov/opendata)");
        }
        List<String> spot = list(ctx, SPOT_PARAM, DEFAULT_SPOT);
        List<String> stock = list(ctx, STOCK_PARAM, DEFAULT_STOCK);
        List<NormalizedRecord> records = new ArrayList<>();
        for (String series : spot) {
            read(ctx, key.strip(), EiaApi.PETROLEUM_SPOT_ROUTE, "petroleum/pri/spt", "daily", "P1D", series,
                    records);
        }
        for (String series : stock) {
            read(ctx, key.strip(), EiaApi.PETROLEUM_STOCKS_ROUTE, "petroleum/stoc/wstk", "weekly", "P1W", series,
                    records);
        }
        ctx.log().debug("{}: {} record(s)", ID, records.size());
        return FetchResult.complete(records);
    }

    private void read(FetchContext ctx, String key, String route, String dataset, String eiaFrequency,
            String frequencyCode, String series, List<NormalizedRecord> out) throws AdapterException {
        EiaApi.Query query = EiaApi.query()
                .add(EiaApi.KEY_PARAM, key)
                .add("frequency", eiaFrequency)
                .add("data[0]", "value")
                .add("facets[series][]", series)
                .add("sort[0][column]", "period")
                .add("sort[0][direction]", "desc")
                .add("length", Integer.toString(TRAILING_ROWS));
        URI uri = EiaApi.uri(baseUrl, route, query);
        EiaParser.Envelope envelope = EiaParser.parse(body(ctx, ctx.http().get(uri), uri));
        String seriesId = "eia:" + series;
        String seriesName = EiaApi.SERIES_NAMES.getOrDefault(series, series);
        NormalizedRecord newest = null;
        Instant newestStart = null;
        for (JsonNode row : envelope.data()) {
            String period = JsonProps.text(row, "period");
            Double value = EiaParser.value(row);
            // Weekly EIA periods are labeled by the week's last day; daily ones by the day itself.
            SeriesPeriods.Period bounds = "P1W".equals(frequencyCode)
                    ? SeriesPeriods.weekEnding(period) : SeriesPeriods.parse(frequencyCode, period);
            if (period == null || value == null || bounds == null) {
                continue;
            }
            String unit = EiaApi.ucum(EiaParser.units(row));
            out.add(SeriesPoint.point(seriesId, seriesName, dataset, frequencyCode, period, bounds.start(),
                    bounds.end(), value, unit, null, null, "final", bounds.end()));
            if (newestStart == null || bounds.start().isAfter(newestStart)) {
                newestStart = bounds.start();
                newest = SeriesPoint.latest(seriesId, seriesName, dataset, frequencyCode, period, value, unit, null,
                        "final", bounds.end());
            }
        }
        if (newest != null) {
            out.add(newest);
        }
    }

    private static byte[] body(FetchContext ctx, UpstreamResponse response, URI uri) {
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ctx.sourceId() + ": " + uri.getPath() + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    private static List<String> list(FetchContext ctx, String param, List<String> fallback) {
        List<String> values = ctx.params() == null ? List.of() : ctx.params().stringList(param);
        return values.isEmpty() ? fallback : values;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("EIA petroleum")
                        .description("EIA daily spot prices (WTI Cushing and Brent by default) and the weekly WPSR"
                                + " crude-oil stocks, as time-series records. The spot prices are sourced by EIA from"
                                + " Refinitiv (LSEG) and are served only to this instance's own clients (LOCAL_ONLY)."
                                + " licenceNote: spot prices sourced from Refinitiv via EIA; third-party rights may"
                                + " apply.")
                        .homepage("https://www.eia.gov/opendata/")
                        .attribution(EiaApi.ATTRIBUTION)
                        .licence(EiaApi.LICENCE)
                        .termsUrl(EiaApi.TERMS_URL)
                        .mode(Mode.POLLING)
                        .egress(Egress.LOCAL_ONLY)
                        .credential(CredentialField.required(EiaApi.API_KEY,
                                "EIA API key (free from eia.gov/opendata), sent as the api_key query parameter"))
                        .param(new ai.foresignal.source.spi.ParamSpec(SPOT_PARAM, ParamType.STRING_LIST,
                                "EIA spot series ids from petroleum/pri/spt (daily)", DEFAULT_SPOT, false, List.of()))
                        .param(new ai.foresignal.source.spi.ParamSpec(STOCK_PARAM, ParamType.STRING_LIST,
                                "EIA weekly stock series ids from petroleum/stoc/wstk", DEFAULT_STOCK, false,
                                List.of()))
                        .interval(Duration.ofMinutes(30), Duration.ofHours(1))
                        .defaultBudget(Budget.of(4, 200))
                        .observedAtMode(ObservedAtMode.UPSTREAM))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}

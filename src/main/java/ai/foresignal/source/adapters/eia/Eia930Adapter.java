package ai.foresignal.source.adapters.eia;

import ai.foresignal.source.series.SeriesPeriods;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.JsonProps;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code eia-930} (prompt 093): hourly US grid demand, demand forecast, net generation and total interchange per
 * balancing authority, plus the hourly fuel-type generation mix, as {@code series-point} records.
 *
 * <p>One request per dataset per poll (a 48-hour window is far under the 5,000-row cap); {@code start} is set
 * explicitly so a revision is re-read. The demand forecast ({@code DF}) is marked {@code status=forecast}. The time
 * base is {@code RECEIVED}, because the feed mixes forecasts and actuals and recent hours are revised.
 */
@Component
public class Eia930Adapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = EiaApi.EIA930_ID;

    /** The respondents parameter. */
    public static final String RESPONDENTS_PARAM = "respondents";

    /** The fuel-mix parameter. */
    public static final String FUEL_MIX_PARAM = "fuelMix";

    /** The default respondents: the US lower-48 aggregate and the largest balancing authorities. */
    public static final List<String> DEFAULT_RESPONDENTS =
            List.of("US48", "ERCO", "CISO", "PJM", "MISO", "NYIS", "ISNE", "SWPP");

    /** The trailing window. */
    static final Duration WINDOW = Duration.ofHours(48);

    /** The window, formatted {@code YYYY-MM-DDTHH} (UTC), as EIA expects it. */
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH").withZone(ZoneOffset.UTC);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public Eia930Adapter(@Value("${" + EiaApi.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
            throw new AdapterException(ID + ": the credential " + EiaApi.API_KEY + " is not set");
        }
        List<String> respondents = ctx.params() == null || ctx.params().stringList(RESPONDENTS_PARAM).isEmpty()
                ? DEFAULT_RESPONDENTS : ctx.params().stringList(RESPONDENTS_PARAM);
        boolean fuelMix = ctx.params() == null || ctx.params().bool(FUEL_MIX_PARAM, true);
        String start = HOUR.format(ctx.now().minus(WINDOW));
        List<NormalizedRecord> points = new ArrayList<>();
        readRegions(ctx, key.strip(), respondents, start, points);
        if (fuelMix) {
            readFuel(ctx, key.strip(), respondents, start, points);
        }
        List<NormalizedRecord> records = new ArrayList<>(points);
        records.addAll(SeriesPoint.latestPerSeries(points));
        ctx.log().debug("{}: {} record(s)", ID, records.size());
        return FetchResult.complete(records);
    }

    private void readRegions(FetchContext ctx, String key, List<String> respondents, String start,
            List<NormalizedRecord> out) throws AdapterException {
        EiaApi.Query query = EiaApi.query()
                .add(EiaApi.KEY_PARAM, key)
                .add("frequency", "hourly")
                .add("data[0]", "value")
                .addAll("facets[respondent][]", respondents)
                .addAll("facets[type][]", EiaApi.REGION_TYPES)
                .add("start", start)
                .add("length", "48");
        URI uri = EiaApi.uri(baseUrl, EiaApi.RTO_REGION_ROUTE, query);
        EiaParser.Envelope envelope = EiaParser.parse(body(ctx, ctx.http().get(uri), uri));
        for (JsonNode row : envelope.data()) {
            String respondent = JsonProps.text(row, "respondent");
            String type = JsonProps.text(row, "type");
            String period = JsonProps.text(row, "period");
            Double value = EiaParser.value(row);
            SeriesPeriods.Period bounds = SeriesPeriods.parse("PT1H", period);
            if (respondent == null || type == null || value == null || bounds == null) {
                continue;
            }
            boolean forecast = "DF".equals(type);
            String seriesId = "eia930:" + respondent + ":" + type;
            String seriesName = name(row, respondent) + " " + EiaApi.REGION_TYPE_NAMES.getOrDefault(type, type);
            String unit = EiaApi.ucum(EiaParser.units(row));
            out.add(SeriesPoint.point(seriesId, seriesName, "electricity/rto/region-data", "PT1H", period,
                    bounds.start(), bounds.end(), value, unit, respondent, null, forecast ? "forecast" : "final",
                    bounds.end()));
        }
    }

    private void readFuel(FetchContext ctx, String key, List<String> respondents, String start,
            List<NormalizedRecord> out) throws AdapterException {
        EiaApi.Query query = EiaApi.query()
                .add(EiaApi.KEY_PARAM, key)
                .add("frequency", "hourly")
                .add("data[0]", "value")
                .addAll("facets[respondent][]", respondents)
                .addAll("facets[fueltype][]", EiaApi.FUEL_TYPES)
                .add("start", start)
                .add("length", "48");
        URI uri = EiaApi.uri(baseUrl, EiaApi.RTO_FUEL_ROUTE, query);
        EiaParser.Envelope envelope = EiaParser.parse(body(ctx, ctx.http().get(uri), uri));
        for (JsonNode row : envelope.data()) {
            String respondent = JsonProps.text(row, "respondent");
            String fuel = JsonProps.text(row, "fueltype");
            String period = JsonProps.text(row, "period");
            Double value = EiaParser.value(row);
            SeriesPeriods.Period bounds = SeriesPeriods.parse("PT1H", period);
            if (respondent == null || fuel == null || value == null || bounds == null) {
                continue;
            }
            String seriesId = "eia930:" + respondent + ":fuel:" + fuel;
            String fuelName = JsonProps.text(row, "fueltype-name");
            String seriesName = name(row, respondent) + " "
                    + (fuelName != null ? fuelName + " generation" : fuel + " generation");
            String unit = EiaApi.ucum(EiaParser.units(row));
            out.add(SeriesPoint.point(seriesId, seriesName, "electricity/rto/fuel-type-data", "PT1H", period,
                    bounds.start(), bounds.end(), value, unit, respondent, null, "final", bounds.end()));
        }
    }

    private static String name(JsonNode row, String respondent) {
        String respondentName = JsonProps.text(row, "respondent-name");
        return respondentName != null ? respondentName : respondent;
    }

    private static byte[] body(FetchContext ctx, UpstreamResponse response, URI uri) {
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ctx.sourceId() + ": " + uri.getPath() + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("EIA-930 grid")
                        .description("Hourly US bulk-power data from EIA-930: demand, demand forecast, net"
                                + " generation and total interchange per balancing authority, plus the hourly"
                                + " fuel-type generation mix, as time-series records (48-hour trailing window).")
                        .homepage("https://www.eia.gov/electricity/gridmonitor/")
                        .attribution(EiaApi.ATTRIBUTION)
                        .licence(EiaApi.LICENCE)
                        .termsUrl(EiaApi.TERMS_URL)
                        .mode(Mode.POLLING)
                        .credential(CredentialField.required(EiaApi.API_KEY,
                                "EIA API key (free from eia.gov/opendata), sent as the api_key query parameter"))
                        .param(new ParamSpec(RESPONDENTS_PARAM, ParamType.STRING_LIST,
                                "Balancing-authority codes (US48 and the largest RTOs by default)", DEFAULT_RESPONDENTS,
                                false, List.of()))
                        .param(ParamSpec.optional(FUEL_MIX_PARAM, ParamType.BOOLEAN,
                                "Also read the hourly fuel-type generation mix", true))
                        .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                        .defaultBudget(Budget.of(4, 200))
                        .observedAtMode(ObservedAtMode.RECEIVED))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}

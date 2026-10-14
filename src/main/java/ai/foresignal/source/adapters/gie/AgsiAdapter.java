package ai.foresignal.source.adapters.gie;

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
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code gie-agsi} (prompt 093): daily EU gas-storage levels from GIE AGSI as {@code series-point} records.
 *
 * <p>Published at 19:30 CET, with a second run at 23:00, for the previous gas day. One point per area and gas day:
 * {@code seriesId = agsi:{area}}, {@code value = full} (%). The key is sent as the {@code x-key} header; AGSI
 * answers a missing or invalid key with HTTP 200 and an {@code error} body, which the adapter turns into a rejected
 * credential.
 */
@Component
public class AgsiAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gie-agsi";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://agsi.gie.eu";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gie.base-url";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The header carrying the key. */
    public static final String KEY_HEADER = "x-key";

    /** The countries parameter. */
    public static final String COUNTRIES_PARAM = "countries";

    /** The default areas. */
    public static final List<String> DEFAULT_COUNTRIES = List.of("DE", "FR", "IT", "NL", "AT");

    /** The largest page AGSI allows. */
    static final int SIZE = 30;

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public AgsiAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The AGSI request URL: the EU aggregate plus one size-limited page. */
    public URI apiUri() {
        return URI.create(baseUrl + "/api?type=eu&size=" + SIZE);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a key from agsi.gie.eu)");
        }
        List<String> countries = ctx.params() == null || ctx.params().stringList(COUNTRIES_PARAM).isEmpty()
                ? DEFAULT_COUNTRIES : ctx.params().stringList(COUNTRIES_PARAM);
        UpstreamResponse response = ctx.http().get(apiUri(), Map.of(KEY_HEADER, key.strip(),
                "Accept", "application/json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": AGSI" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        byte[] body = response.body();
        AgsiParser.checkAuth(body);
        List<NormalizedRecord> all = AgsiParser.parse(body);
        Set<String> wanted = new TreeSet<>();
        countries.forEach(c -> wanted.add(c.strip().toUpperCase(Locale.ROOT)));
        List<NormalizedRecord> filtered = all.stream().filter(r -> keep(r, wanted)).toList();
        List<NormalizedRecord> records = filtered.isEmpty() ? all : filtered;
        List<NormalizedRecord> out = new ArrayList<>(records);
        out.addAll(SeriesPoint.latestPerSeries(records));
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        return FetchResult.complete(out);
    }

    private static boolean keep(NormalizedRecord record, Set<String> countries) {
        String area = String.valueOf(record.payload().get("area")).toUpperCase(Locale.ROOT);
        return countries.contains(area) || "EU".equals(area);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("GIE AGSI gas storage")
                        .description("Daily EU gas-storage levels from GIE AGSI: one point per area and gas day,"
                                + " value = full (%), with the storage, injection, withdrawal and working-volume"
                                + " numbers as extras. Needs a free AGSI API key.")
                        .homepage("https://agsi.gie.eu/")
                        .attribution("GIE AGSI")
                        .licence("GIE AGSI data usage rules (any use incl. commercial, mandatory credit)")
                        .termsUrl("https://agsi.gie.eu/data-usage")
                        .mode(Mode.POLLING)
                        .credential(CredentialField.required(API_KEY,
                                "GIE AGSI API key (free from agsi.gie.eu), sent as the x-key header"))
                        .param(new ParamSpec(COUNTRIES_PARAM, ParamType.STRING_LIST,
                                "Area codes to keep (the EU aggregate is always kept)", DEFAULT_COUNTRIES, false,
                                List.of()))
                        .interval(Duration.ofHours(1), Duration.ofHours(3))
                        .defaultBudget(Budget.of(2, 40))
                        .observedAtMode(ObservedAtMode.UPSTREAM))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}

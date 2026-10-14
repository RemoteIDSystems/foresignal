package ai.foresignal.source.adapters.deepstate;

import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
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
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code deepstate-frontline} (prompt 081): the daily DeepStateMap occupied area ({@link DeepStateParser}).
 *
 * <ul>
 *   <li><strong>File:</strong> {@value #FILE_URL} with the mirror {@code ref} (param {@value #REF_PARAM}, default
 *       {@value #DEFAULT_REF}) and the file date. Today's UTC date is tried first and, on a 404 (the mirror publishes at
 *       about 03:00 UTC), yesterday's; each file name is fetched conditionally (state key {@code deepstate-{yyyyMMdd}};
 *       the validators of older names are forgotten). A 304 is {@link FetchResult#unchanged()}.
 *   <li><strong>Pinned data:</strong> a 40-hex commit as {@code ref} pins the data; {@value #FILE_DATE_PARAM} is then
 *       required (a pinned commit has a fixed newest file), the file is fetched once, and every later run returns
 *       not-modified without a request. A {@code fileDate} with a branch ref reads that day's file only.
 *   <li><strong>History:</strong> each daily map with a changed geometry is a new version of {@value
 *       DeepStateParser#EXTERNAL_ID}, so {@code asOf} reads and version history show the front over time.
 *       {@code areaChangeKm2} compares with the previous map date, kept in state ({@value #CURRENT_KEY},
 *       {@value #PREVIOUS_KEY}: {@code yyyy-MM-dd|areaKm2}).
 * </ul>
 */
@Component
public class DeepStateAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "deepstate-frontline";

    /** The daily file, with {@code {ref}} and {@code {yyyyMMdd}}. */
    public static final String FILE_URL =
            "https://raw.githubusercontent.com/cyterat/deepstate-map-data/{ref}/data/deepstatemap_data_{yyyyMMdd}.geojson";

    /** The ref parameter. */
    public static final String REF_PARAM = "ref";

    /** The file date parameter. */
    public static final String FILE_DATE_PARAM = "fileDate";

    /** The default ref. */
    public static final String DEFAULT_REF = "main";

    /** The state key of the newest map read: {@code yyyy-MM-dd|areaKm2}. */
    public static final String CURRENT_KEY = "deepstate:current";

    /** The state key of the map before it: {@code yyyy-MM-dd|areaKm2}. */
    public static final String PREVIOUS_KEY = "deepstate:previous";

    /** The state key of the pinned file read: {@code ref|yyyy-MM-dd}. */
    public static final String PINNED_KEY = "deepstate:pinned";

    /** The prefix of the conditional GET state key of one file name. */
    public static final String FILE_STATE_PREFIX = "deepstate-";

    /** A 40-hex commit. */
    static final Pattern SHA = Pattern.compile("^[0-9a-fA-F]{40}$");

    /** A branch or tag name the URL path accepts. */
    static final Pattern REF = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.BASIC_ISO_DATE;

    private final String fileUrl;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value ConflictSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public DeepStateAdapter(@Value("${" + ConflictSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.fileUrl = ConflictSources.uri(FILE_URL.replace("{ref}", "REF").replace("{yyyyMMdd}", "DATE"),
                baseUrlOverride).toString();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The file URL of {@code ref} and {@code date}. */
    public URI fileUri(String ref, LocalDate date) {
        return URI.create(fileUrl.replace("REF", ref).replace("DATE", date.format(COMPACT)));
    }

    /** The conditional GET state key of one file name. */
    public static String stateKey(LocalDate date) {
        return FILE_STATE_PREFIX + date.format(COMPACT);
    }

    /** True for a 40-hex commit. */
    public static boolean pinned(String ref) {
        return ref != null && SHA.matcher(ref).matches();
    }

    /**
     * {@code ref} must be a branch name or a commit; {@code fileDate} must be a date ({@code yyyy-MM-dd} or
     * {@code yyyyMMdd}) and is required with a commit.
     */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String ref = params.has(REF_PARAM) ? params.string(REF_PARAM) : DEFAULT_REF;
        if (ref == null || !REF.matcher(ref).matches() || ref.contains("..")) {
            problems.put(REF_PARAM, "must be a branch name or a 40-hex commit, but was '" + ref + "'");
        }
        String fileDate = params.has(FILE_DATE_PARAM) ? params.string(FILE_DATE_PARAM) : null;
        if (fileDate != null && parseDate(fileDate) == null) {
            problems.put(FILE_DATE_PARAM, "must be a date (yyyy-MM-dd), but was '" + fileDate + "'");
        }
        if (pinned(ref) && fileDate == null) {
            problems.put(FILE_DATE_PARAM, "is required when ref is a commit (a pinned commit has a fixed newest file)");
        }
        return problems;
    }

    /**
     * Fetches the newest daily map (or the pinned one) and maps it.
     *
     * @throws AdapterException when both days answer 404, or the request fails otherwise
     * @throws UpstreamFormatException when the file does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        AdapterStateStore state = ctx.state();
        String ref = ctx.params().has(REF_PARAM) ? ctx.params().string(REF_PARAM) : DEFAULT_REF;
        LocalDate fixed = ctx.params().has(FILE_DATE_PARAM) ? parseDate(ctx.params().string(FILE_DATE_PARAM)) : null;
        if (pinned(ref)) {
            if (fixed == null) {
                throw new AdapterException(ID + ": " + FILE_DATE_PARAM + " is required when ref is a commit");
            }
            String marker = ref + "|" + fixed;
            if (marker.equals(state.get(PINNED_KEY).orElse(null))) {
                return FetchResult.unchanged();
            }
            URI uri = fileUri(ref, fixed);
            byte[] body = ConflictSources.body(ctx, ctx.http().get(uri), null, uri);
            NormalizedRecord record = map(state, body, fixed, ref, null);
            state.put(PINNED_KEY, marker);
            return FetchResult.complete(List.of(record));
        }
        state.remove(PINNED_KEY);
        List<LocalDate> dates = fixed != null ? List.of(fixed)
                : List.of(LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC),
                        LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC).minusDays(1));
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            URI uri = fileUri(ref, date);
            String key = stateKey(date);
            UpstreamResponse response;
            try {
                response = ctx.http().getConditional(uri, key);
            } catch (AdapterException e) {
                if (e.httpStatus().orElse(0) == 404 && i + 1 < dates.size()) {
                    ctx.log().debug("{}: no map for {} yet; trying {}", ID, date, dates.get(i + 1));
                    continue;
                }
                throw e;
            }
            forgetOlder(state, date);
            if (response.notModified()) {
                return FetchResult.unchanged();
            }
            byte[] body = ConflictSources.body(ctx, response, key, uri);
            return FetchResult.complete(List.of(map(state, body, date, ref, key)));
        }
        throw new AdapterException(ID + ": no daily map was found");
    }

    /** Parses a file, works out the area change against the previous map, and records the map in state. */
    private static NormalizedRecord map(AdapterStateStore state, byte[] body, LocalDate date, String ref, String key) {
        DeepStateParser.MapFile map;
        try {
            map = DeepStateParser.parse(body);
        } catch (UpstreamFormatException e) {
            if (key != null) {
                SourceHttp.forgetValidators(state, key);
            }
            throw e;
        }
        Stamp current = Stamp.parse(state.get(CURRENT_KEY).orElse(null));
        Stamp previous = Stamp.parse(state.get(PREVIOUS_KEY).orElse(null));
        Stamp read = new Stamp(date, map.areaKm2());
        if (current == null || date.isAfter(current.date())) {
            if (current != null) {
                state.put(PREVIOUS_KEY, current.text());
            }
            previous = current;
            state.put(CURRENT_KEY, read.text());
        } else if (date.equals(current.date())) {
            state.put(CURRENT_KEY, read.text());
        } else {
            previous = null; // an older map than the newest read: no meaningful change
        }
        Double change = previous == null ? null : map.areaKm2() - previous.areaKm2();
        return DeepStateParser.record(map, date, ref, change);
    }

    /** Forgets the validators of the file names two and three days before {@code date}. */
    private static void forgetOlder(AdapterStateStore state, LocalDate date) {
        for (int days = 2; days <= 3; days++) {
            String key = stateKey(date.minusDays(days));
            if (state.get(SourceHttp.ETAG_STATE_PREFIX + key).isPresent()
                    || state.get(SourceHttp.LAST_MODIFIED_STATE_PREFIX + key).isPresent()) {
                SourceHttp.forgetValidators(state, key);
            }
        }
    }

    /** A date {@code yyyy-MM-dd} or {@code yyyyMMdd}, or null. */
    static LocalDate parseDate(String text) {
        String value = ConflictSources.trimmed(text);
        if (value == null) {
            return null;
        }
        try {
            return value.length() == 8 ? LocalDate.parse(value, COMPACT) : LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** A map date and its area, as stored. */
    record Stamp(LocalDate date, double areaKm2) {

        String text() {
            return date + "|" + areaKm2;
        }

        static Stamp parse(String text) {
            if (text == null) {
                return null;
            }
            int bar = text.indexOf('|');
            if (bar <= 0) {
                return null;
            }
            try {
                return new Stamp(LocalDate.parse(text.substring(0, bar)), Double.parseDouble(text.substring(bar + 1)));
            } catch (DateTimeParseException | NumberFormatException e) {
                return null;
            }
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = DeepStateParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("DeepStateMap occupied area (Ukraine)")
                .description("The occupied area of Ukraine as drawn daily by DeepStateMap.Live, read from the GitHub"
                        + " mirror cyterat/deepstate-map-data (one MultiPolygon per day, published at about 03:00"
                        + " UTC). A single entity, occupied-area, whose versions show the front over time; areaKm2 is"
                        + " the area on the WGS84 ellipsoid and areaChangeKm2 the change against the previous map."
                        + " Param ref pins the data to a mirror commit (then fileDate is required). The mirror"
                        + " repository is GPL-3.0 (only its data files are downloaded); DeepStateMap publishes no"
                        + " data terms. A map is one analyst group's assessment, not ground truth.")
                .kind(kind)
                .homepage("https://github.com/cyterat/deepstate-map-data")
                .attribution("DeepStateMap.Live via cyterat/deepstate-map-data")
                .licence("GPL-3.0 mirror; DeepStateMap terms unspecified")
                .termsUrl("https://github.com/cyterat/deepstate-map-data/blob/main/LICENSE")
                .mode(Mode.POLLING)
                .param(ParamSpec.optional(REF_PARAM, ParamType.STRING, "The mirror branch, or a 40-hex commit to pin"
                        + " the data", DEFAULT_REF))
                .param(ParamSpec.optional(FILE_DATE_PARAM, ParamType.STRING, "The file date (yyyy-MM-dd); required"
                        + " when ref is a commit, otherwise reads that day's file only", null))
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(2, 20))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(ConflictSources.EVENT_RETENTION)
                .kindRetention(kind, ConflictSources.EVENT_RETENTION)
                .egress(Egress.OPEN)
                .titleField("mapDate")
                .tileProperties("mapDate", "areaKm2", "areaChangeKm2")
                .searchFields("mapDate")
                .rankField("areaKm2");
        ConflictSources.text(b, "mapDate", "Map date", "The date of the daily map (yyyy-MM-dd)");
        ConflictSources.number(b, "areaKm2", "km2", "Occupied area", "The area on the WGS84 ellipsoid, 0.1 km²");
        ConflictSources.number(b, "areaChangeKm2", "km2", "Change", "The change against the previous map, km²"
                + " (positive: more territory occupied)");
        ConflictSources.text(b, "ref", "Mirror ref", "The mirror branch or commit read");
        ConflictSources.bool(b, "geometrySimplified", "Simplified", "True when the polygon was simplified to fit"
                + " the vertex limit");
        ConflictSources.number(b, "simplifyToleranceDeg", "deg", "Simplification tolerance", "The tolerance used,"
                + " degrees");
        b.kindDisplay(kind, new KindDisplay("Occupied area {mapDate}", null, "#C92A2A", null));
        return b.build();
    }
}

package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code who-don} (prompt 090): WHO Disease Outbreak News.
 *
 * <p>The title is {@code <disease> – <country>}; the country (or, for a multi-country title, each country) is
 * resolved through prompt 040's gazetteer, and a multi-country record is a MultiPoint of label points flagged
 * {@code geometryApproximate}.
 */
@Component
public final class WhoDonAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "who-don";

    /** The record kind. */
    public static final String KIND = "disease-outbreak";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://www.who.int";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.who-don.base-url";

    static final Duration RETENTION = Duration.ofDays(1825);

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final URI baseUrl;
    private final CountryCentroids countries;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public WhoDonAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride, CountryCentroids countries) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.countries = countries;
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(URI.create(baseUrl
                + "/api/emergencies/diseaseoutbreaknews?$orderby=PublicationDate desc&$top=50"));
        if (response.status() != 200) {
            throw new UpstreamFormatException("WHO DON answered HTTP " + response.status(), "");
        }
        return FetchResult.of(parse(response.body()));
    }

    /** The records of a WHO DON OData body ({@code value} array). */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode value = JSON.readTree(body).get("value");
        List<NormalizedRecord> out = new ArrayList<>();
        if (value == null || !value.isArray()) {
            return out;
        }
        for (JsonNode item : value) {
            NormalizedRecord record = record(item);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode item) {
        String id = text(item, "Id");
        if (id == null) {
            return null;
        }
        String title = Boolean.TRUE.equals(bool(item, "UseOverrideTitle"))
                ? firstNonNull(text(item, "OverrideTitle"), text(item, "Title"))
                : text(item, "Title");
        String[] split = splitTitle(title);
        String disease = split[0];
        List<String> resolved = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        if (split[1] != null) {
            for (String name : split[1].split("\\s*(?:,| and | & )\\s*")) {
                if (name.isBlank()) {
                    continue;
                }
                Optional<ai.foresignal.reference.CountryRef> ref =
                        countries.byName(name.replaceFirst("(?i)^the\\s+", ""));
                ref.ifPresent(country -> {
                    resolved.add(country.iso2());
                    points.add(Geo.point(country.labelLon(), country.labelLat()));
                });
            }
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(time(text(item, "LastModified")))
                .eventTime(time(text(item, "PublicationDate")));
        Geometry geometry = points.size() == 1 ? points.get(0)
                : points.isEmpty() ? null : FACTORY.createMultiPoint(points.toArray(new Point[0]));
        if (geometry != null) {
            b.geometry(geometry);
            b.put("geometryApproximate", true);
        }
        b.put("donId", text(item, "DonId"));
        b.put("title", title);
        b.put("disease", disease);
        b.put("countries", resolved);
        b.put("url", text(item, "UrlName") == null ? null
                : "https://www.who.int/emergencies/disease-outbreak-news/item/" + text(item, "UrlName"));
        b.put("summary", HtmlText.strip(text(item, "Summary"), 4 * 1024));
        return b.build();
    }

    /** {@code {disease, countryText}} from a title split on the last dash. */
    static String[] splitTitle(String title) {
        if (title == null) {
            return new String[] {null, null};
        }
        int sep = Math.max(title.lastIndexOf(" – "), title.lastIndexOf(" - "));
        if (sep < 0) {
            return new String[] {title.strip(), null};
        }
        String separator = title.startsWith(" – ", sep) ? " – " : " - ";
        return new String[] {title.substring(0, sep).strip(), title.substring(sep + separator.length()).strip()};
    }

    private static String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    static Instant time(String value) {
        return Instants.parse(value);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("WHO Disease Outbreak News")
                .description("Disease Outbreak News from the World Health Organization. The title is "
                        + "'<disease> – <country>'; countries resolve to 040 label points (approximate).")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.who.int/emergencies/disease-outbreak-news")
                .attribution("World Health Organization")
                .licence("CC BY-NC-SA 3.0 IGO (WHO)")
                .termsUrl("https://www.who.int/about/policies/publishing/copyright")
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(2, 50)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("title")
                .searchFields("title", "disease", "summary")
                .embedFields("title", "summary")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{title}", "hospital", "#C2255C", null))
                .build();
    }
}

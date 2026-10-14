package ai.foresignal.source.adapters.faa;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Temporary flight restrictions from tfr.faa.gov ({@code faa-tfr}, prompt 087), with their XNOTAM polygons and
 * validity windows.
 *
 * <p><strong>List:</strong> {@code GET https://tfr.faa.gov/tfrapi/exportTfrList}, a JSON array of
 * {@code {notam_id, type, facility, state, description, creation_date}} served {@code Cache-Control: no-cache} with no
 * validators (verified 2026-10-14: 76 TFRs). Each row is hashed (SHA-256 over its fields in name order).
 *
 * <p><strong>Details:</strong> {@code GET https://tfr.faa.gov/download/detail_{notam_id with "/" as "_"}.xml}, only
 * for a row that is new, whose hash changed, or whose detail is older than {@value #REFRESH_HOURS} hours (so a
 * record lost downstream is re-sent daily), at most {@value #MAX_DETAILS} per run, one after another. The adapter
 * state keeps {@code id → {hash, fetched}} ({@value #ROWS_KEY}); a row left over by the cap, or whose detail is
 * unreadable or answered 404/410, is fetched on a later run (any other HTTP error fails the run, a 429 with the
 * standard backoff).
 *
 * <p><strong>Records</strong> ({@link XnotamParser}): kind {@code airspace-restriction}, externalId {@code notam_id},
 * {@code observedAt} = {@code dateIssued} (else the list's {@code creation_date}, else now), {@code validFrom} =
 * {@code dateEffective}, {@code validUntil} = {@code dateExpire} (upstream-asserted; none means "until further
 * notice"). A row that leaves the list is tombstoned at once. Unchanged rows emit nothing: their records stay current.
 * 15-minute poll, budget 45/min and 2,000/day, retention 1825 days ({@code event}: NOTAMs and TFRs).
 */
@Component
public class FaaTfrAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "faa-tfr";

    /** The record kind. */
    public static final String KIND = "airspace-restriction";

    /** The list URL. */
    public static final String LIST_URL = "https://tfr.faa.gov/tfrapi/exportTfrList";

    /** The detail URL prefix; the id with {@code /} replaced by {@code _} and {@code .xml} follow. */
    public static final String DETAIL_URL_PREFIX = "https://tfr.faa.gov/download/detail_";

    /** Detail fetches per run. */
    public static final int MAX_DETAILS = 40;

    /** Hours after which a detail is fetched again although its row is unchanged. */
    public static final int REFRESH_HOURS = 24;

    /** The adapter-state key of the known rows ({@code {id: {"h": hash, "t": epochSeconds}}}). */
    public static final String ROWS_KEY = "tfr:rows";

    private static final DateTimeFormatter CREATION_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value FaaSources#BASE_URL_PROPERTY}; blank in production
     */
    public FaaTfrAdapter(@Value("${" + FaaSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The list URL, rebased in tests. */
    URI listUri() {
        return FaaSources.uri(LIST_URL, baseUrl);
    }

    /** The detail URL of {@code notamId} ({@code 6/8784} → {@code detail_6_8784.xml}), rebased in tests. */
    URI detailUri(String notamId) {
        return FaaSources.uri(detailUrl(notamId), baseUrl);
    }

    /** The production detail URL of {@code notamId}. */
    public static String detailUrl(String notamId) {
        return DETAIL_URL_PREFIX + notamId.replace('/', '_') + ".xml";
    }

    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        JsonNode list = FaaSources.json(FaaSources.body(ctx.http().get(listUri()), "TFR list"), "TFR list");
        if (!list.isArray()) {
            throw new UpstreamFormatException("TFR list is not a JSON array", "");
        }
        Map<String, JsonNode> rows = new LinkedHashMap<>();
        for (JsonNode row : list) {
            String id = FaaSources.text(row.get("notam_id"));
            if (id != null && !id.contains("..") && id.matches("^[0-9A-Za-z/_-]{1,32}$")) {
                rows.putIfAbsent(id, row);
            }
        }
        Map<String, Known> known = readKnown(ctx.state().get(ROWS_KEY).orElse(null));
        Instant now = ctx.now();
        List<NormalizedRecord> records = new ArrayList<>();
        int fetched = 0;
        int failed = 0;
        boolean capped = false;
        for (Map.Entry<String, JsonNode> e : rows.entrySet()) {
            String id = e.getKey();
            String hash = rowHash(e.getValue());
            Known k = known.get(id);
            boolean due = k == null || !k.hash().equals(hash)
                    || k.fetched().plus(Duration.ofHours(REFRESH_HOURS)).isBefore(now);
            if (!due) {
                continue;
            }
            if (fetched >= MAX_DETAILS) {
                capped = true;
                continue;
            }
            fetched++;
            XnotamParser.Tfr tfr;
            try {
                tfr = XnotamParser.parse(FaaSources.body(ctx.http().get(detailUri(id)), "TFR detail " + id));
            } catch (UpstreamFormatException ex) {
                failed++;
                ctx.log().debug("{}: detail {} unusable: {}", ctx.sourceId(), id, ex.getMessage());
                continue;
            } catch (UpstreamHttpException ex) {
                if (ex.getStatus() != 404 && ex.getStatus() != 410) {
                    throw ex;
                }
                failed++;
                ctx.log().debug("{}: detail {} answered {}", ctx.sourceId(), id, ex.getStatus());
                continue;
            }
            records.add(record(id, e.getValue(), tfr, now));
            known.put(id, new Known(hash, now));
        }
        List<String> gone = new ArrayList<>();
        for (String id : List.copyOf(known.keySet())) {
            if (!rows.containsKey(id)) {
                gone.add(id);
                known.remove(id);
                records.add(NormalizedRecord.tombstone(id, KIND, now));
            }
        }
        ctx.state().put(ROWS_KEY, FaaSources.pack(writeKnown(known)));
        ctx.log().debug("{}: {} row(s), {} detail(s) fetched, {} failed, {} tombstone(s){}", ctx.sourceId(),
                rows.size(), fetched, failed, gone.size(), capped ? ", detail cap reached" : "");
        if (records.isEmpty()) {
            return FetchResult.unchanged();
        }
        return capped || failed > 0 ? FetchResult.of(records) : FetchResult.complete(records);
    }

    /** The record of one TFR. */
    static NormalizedRecord record(String id, JsonNode row, XnotamParser.Tfr tfr, Instant now) {
        Instant observed = tfr.issued() != null ? tfr.issued() : creationDate(FaaSources.text(row.get("creation_date")));
        Instant from = tfr.effective();
        Instant until = tfr.expires();
        if (from != null && until != null && until.isBefore(from)) {
            from = null;
        }
        String state = FaaSources.text(row.get("state"));
        String facility = FaaSources.text(row.get("facility"));
        String description = tfr.description() != null ? tfr.description() : FaaSources.text(row.get("description"));
        return NormalizedRecord.builder(id, KIND)
                .observedAt(observed == null ? now : observed)
                .validFrom(from)
                .validUntil(until)
                .geometry(tfr.geometry())
                .put("notamId", id)
                .put("tfrType", FaaSources.text(row.get("type")))
                .put("facility", facility != null ? facility : tfr.facility())
                .put("state", state != null ? state : tfr.stateName())
                .put("city", tfr.city())
                .put("regulation", tfr.regulation())
                .put("lowerFt", tfr.lowerFt())
                .put("upperFt", tfr.upperFt())
                .put("lowerRef", tfr.lowerRef())
                .put("upperRef", tfr.upperRef())
                .put("description", description)
                .build();
    }

    /** The list's {@code creation_date} ({@code 10/14/2026}) as UTC midnight, or null. */
    static Instant creationDate(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text, CREATION_DATE).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The hash of a list row: SHA-256 over its scalar fields in name order, the first 16 hex digits. */
    static String rowHash(JsonNode row) {
        Map<String, String> fields = new TreeMap<>();
        row.properties().forEach(e -> fields.put(e.getKey(), e.getValue().isNull() ? "" : e.getValue().asString()));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            fields.forEach((k, v) -> {
                digest.update(k.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(v.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 1);
            });
            return HexFormat.of().formatHex(digest.digest()).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java platform", e);
        }
    }

    /**
     * A known row.
     *
     * @param hash the row hash of the last fetched detail
     * @param fetched when that detail was fetched
     */
    record Known(String hash, Instant fetched) {}

    /** The known rows stored under {@value #ROWS_KEY}; a missing or damaged value reads as none. */
    static Map<String, Known> readKnown(String value) {
        Map<String, Known> out = new LinkedHashMap<>();
        String text = FaaSources.unpack(value);
        if (text == null || text.isBlank()) {
            return out;
        }
        try {
            JsonNode root = FaaSources.JSON.readTree(text);
            root.properties().forEach(e -> {
                JsonNode h = e.getValue().get("h");
                JsonNode t = e.getValue().get("t");
                if (h != null && t != null && t.isNumber()) {
                    out.put(e.getKey(), new Known(h.asString(), Instant.ofEpochSecond(t.longValue())));
                }
            });
        } catch (RuntimeException e) {
            out.clear();
        }
        return out;
    }

    /** The known rows as stored under {@value #ROWS_KEY}. */
    static String writeKnown(Map<String, Known> known) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        known.forEach((id, k) -> {
            ObjectNode node = root.putObject(id);
            node.put("h", k.hash());
            node.put("t", k.fetched().getEpochSecond());
        });
        return root.toString();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("FAA temporary flight restrictions")
                .description("Temporary flight restrictions from tfr.faa.gov with their XNOTAM polygons, vertical"
                        + " limits and validity windows. Details are fetched only for new or changed TFRs (at most 40"
                        + " per run) and refreshed daily; a TFR that leaves the list is tombstoned.")
                .kind(KIND)
                .homepage("https://tfr.faa.gov")
                .attribution(FaaSources.ATTRIBUTION)
                .licence(FaaSources.LICENSE)
                .termsUrl("https://www.faa.gov/web_policies")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(45, 2000))
                .maxResponseBytes(4 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(FaaSources.EVENT_RETENTION)
                .kindRetention(KIND, FaaSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .titleField("description")
                .searchFields("description", "notamId", "city")
                .embedFields("description")
                .tileProperties("tfrType", "lowerFt", "upperFt")
                .kindDisplay(KIND, new KindDisplay("TFR {notamId}", "danger", "#C92A2A", null));
        FaaSources.text(b, "notamId", "NOTAM", "The FDC NOTAM number, for example 6/8784");
        FaaSources.text(b, "tfrType", "Type", "The TFR type from the list: SECURITY, HAZARDS, VIP, SPACE OPERATIONS,"
                + " AIR SHOWS/SPORTS, UAS PUBLIC GATHERING, SPECIAL");
        FaaSources.text(b, "facility", "Facility", "The controlling ARTCC, for example ZLC");
        FaaSources.text(b, "state", "State", "The state, a two-letter code from the list");
        FaaSources.text(b, "city", "Location", "The affected location (txtNameCity)");
        FaaSources.text(b, "regulation", "Regulation", "The 14 CFR section, for example 91.137(a)(1) or 99.7");
        FaaSources.integer(b, "lowerFt", "[ft_i]", "Floor", "The lowest lower limit of the areas, feet");
        FaaSources.integer(b, "upperFt", "[ft_i]", "Ceiling", "The highest upper limit of the areas, feet");
        FaaSources.text(b, "lowerRef", "Floor reference", "SFC, ALT (MSL) or HEI (above ground)");
        FaaSources.text(b, "upperRef", "Ceiling reference", "SFC, ALT (MSL) or HEI (above ground)");
        FaaSources.text(b, "description", "Text", "The NOTAM text (upstream text, untrusted)");
        return b.build();
    }
}

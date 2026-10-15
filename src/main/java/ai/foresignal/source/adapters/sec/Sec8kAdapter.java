package ai.foresignal.source.adapters.sec;

import ai.foresignal.source.adapters.humanitarian.HtmlText;
import ai.foresignal.source.adapters.humanitarian.Instants;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialField;
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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code sec-8k} (prompt 091): SEC EDGAR's current 8-K Atom feed, up to three pages per run.
 *
 * <p>The SEC requires a declared User-Agent with contact details; the {@code contactEmail} param is appended
 * to Foresignal's own User-Agent (never another product's). The feed is rolling, so it never tombstones
 * ({@code complete=false}).
 */
@Component
public final class Sec8kAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "sec-8k";

    /** The record kind. */
    public static final String KIND = "filing";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://www.sec.gov";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.sec-8k.base-url";

    /** The contact email setting used for the User-Agent (the SEC requires contact details). */
    public static final String CONTACT_PROPERTY = "foresignal.adapters.sec-8k.contact-email";

    /** The credential name. */
    public static final String CONTACT_EMAIL = "contactEmail";

    static final String PATH = "/cgi-bin/browse-edgar?action=getcurrent&type=8-K&output=atom&count=100";
    static final Duration RETENTION = Duration.ofDays(1825);
    static final int MAX_PAGES = 3;

    static final Pattern TITLE = Pattern.compile("^(\\S+)\\s*-\\s*(.+?)\\s*\\((\\d{10})\\)\\s*\\(Filer\\)");
    // The summary is escaped HTML (`<b>Filed:</b> …`), decoded by Xml; a label is followed by the
    // closing </b> before its value, so allow any non-digit run between the label and the value.
    static final Pattern ACC_NO = Pattern.compile("AccNo:[^\\d]*([\\d-]+)");
    static final Pattern FILED = Pattern.compile("Filed:[^\\d]*(\\d{4}-\\d{2}-\\d{2})");
    static final Pattern SIZE = Pattern.compile("Size:[^\\d]*([\\d.]+)\\s*(KB|MB)");
    static final Pattern ITEM = Pattern.compile("Item\\s+(\\d+\\.\\d+):\\s*([^<\\n]+)");

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public Sec8kAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${" + CONTACT_PROPERTY + ":}") String contactEmail) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.descriptor = buildDescriptor(contactEmail);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> records = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            String suffix = page == 0 ? "" : "&start=" + (page * 100);
            UpstreamResponse response = ctx.http().get(URI.create(baseUrl + PATH + suffix));
            if (response.status() != 200) {
                throw new UpstreamFormatException("The SEC current feed answered HTTP " + response.status(), "");
            }
            List<NormalizedRecord> pageRecords = parse(response.body());
            records.addAll(pageRecords);
            if (pageRecords.size() < 100) {
                break;
            }
        }
        return FetchResult.of(records);
    }

    /** The records of an EDGAR current-8-K Atom page. */
    public List<NormalizedRecord> parse(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        try {
            XMLStreamReader reader = ai.foresignal.source.adapters.sanctions.Xml.openBytes(body);
            while (reader.hasNext()) {
                reader.next();
                if (reader.isStartElement() && "entry".equals(reader.getLocalName())) {
                    NormalizedRecord record = map(ai.foresignal.source.adapters.sanctions.Xml.readElement(reader));
                    if (record != null) {
                        out.add(record);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The SEC current feed is not valid XML", "", e);
        }
        return out;
    }

    private static NormalizedRecord map(ai.foresignal.source.adapters.sanctions.Xml.Node entry) {
        String summary = entry.childText("summary");
        String accNo = first(ACC_NO, summary);
        if (accNo == null) {
            return null;
        }
        String title = entry.childText("title");
        String form = null;
        String company = null;
        String cik = null;
        if (title != null) {
            Matcher titleMatch = TITLE.matcher(title.strip());
            if (titleMatch.find()) {
                form = titleMatch.group(1);
                company = titleMatch.group(2);
                cik = titleMatch.group(3);
            }
        }
        List<String> items = new ArrayList<>();
        List<String> itemTitles = new ArrayList<>();
        if (summary != null) {
            Matcher item = ITEM.matcher(summary);
            while (item.find()) {
                items.add(item.group(1));
                itemTitles.add(HtmlText.strip(item.group(2), 300));
            }
        }
        String url = null;
        for (ai.foresignal.source.adapters.sanctions.Xml.Node link : entry.all("link")) {
            String href = link.attribute("href");
            if (href != null) {
                url = href;
            }
        }
        String filed = first(FILED, summary);

        NormalizedRecord.Builder b = NormalizedRecord.builder(accNo, KIND)
                .observedAt(Instants.parse(entry.childText("updated")))
                .eventTime(filed == null ? null : Instants.parse(filed + "T00:00:00Z"));
        b.put("form", form);
        b.put("company", company);
        b.put("cik", cik);
        b.put("items", items);
        b.put("itemTitles", itemTitles);
        b.put("sizeKb", size(summary));
        b.put("url", url);
        return b.build();
    }

    private static String first(Pattern pattern, String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static Integer size(String summary) {
        if (summary == null) {
            return null;
        }
        Matcher matcher = SIZE.matcher(summary);
        if (!matcher.find()) {
            return null;
        }
        double value = Double.parseDouble(matcher.group(1));
        return (int) Math.round("MB".equals(matcher.group(2)) ? value * 1024 : value);
    }

    private static SourceDescriptor buildDescriptor(String contactEmail) {
        return SourceDescriptor.builder(ID)
                .name("SEC 8-K current filings")
                .description("SEC EDGAR's current 8-K filings feed. Item codes (for example 1.05, 2.02) let a rule "
                        + "watch for a material category. The SEC requires a declared User-Agent with contact "
                        + "details.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.sec.gov/os/accessing-edgar-data")
                .attribution("US Securities and Exchange Commission")
                .licence("US Government work (public domain)")
                .termsUrl("https://www.sec.gov/privacy")
                .credential(CredentialField.required(CONTACT_EMAIL,
                        "Contact e-mail for the SEC User-Agent (SEC requires contact details)"))
                .userAgentContact(contactEmail)
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(10))
                .defaultBudget(6, 600)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("company")
                .searchFields("company", "items")
                .embedFields("company", "itemTitles")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{company} {form}", "industry", "#1971C2", null))
                .build();
    }
}

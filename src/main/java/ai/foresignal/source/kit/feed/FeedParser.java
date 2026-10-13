package ai.foresignal.source.kit.feed;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses RSS 2.0, RSS 1.0 (RDF), Atom 1.0 and JSON Feed 1.0/1.1 into a {@link FeedDocument} (prompt 073). Pure: no
 * network, no clock, no state.
 *
 * <p><strong>Detection:</strong> by content type first ({@code application/feed+json} and {@code application/json}
 * are JSON Feed; any {@code /xml} or {@code +xml} type is XML), then by the first non-blank byte, then by the root
 * element ({@code rss}, {@code rdf:RDF}, {@code feed}) or the JSON Feed {@code version} key.
 *
 * <p><strong>XML</strong> is read through a hardened StAX factory ({@link XmlTree}): no DTDs, no external entities,
 * coalescing on, a {@value #MAX_BYTES}-byte cap. CDATA, {@code content:encoded} and Atom {@code type=html|xhtml|text}
 * are handled; every text becomes plain text ({@link FeedText}).
 *
 * <p><strong>Items:</strong> {@code id} is the guid, Atom id or JSON Feed id, else the first 16 hex characters of
 * SHA-256 of link and title; links are absolute http(s) only; categories at most 20, enclosures at most 5; dates per
 * {@link FeedDates}; geometry per {@link FeedGeometry} (and the JSON Feed {@code _geo} extension, a GeoJSON geometry
 * or Feature).
 */
public final class FeedParser {

    /** The largest feed parsed, in bytes. */
    public static final int MAX_BYTES = 8 * 1024 * 1024;

    /** Atom. */
    static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    /** RSS 1.0. */
    static final String RSS1_NS = "http://purl.org/rss/1.0/";

    /** RDF. */
    static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** Dublin Core elements. */
    static final String DC_NS = "http://purl.org/dc/elements/1.1/";

    /** RSS content module. */
    static final String CONTENT_NS = "http://purl.org/rss/1.0/modules/content/";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FeedParser() {}

    /**
     * Parses a feed.
     *
     * @param body the bytes
     * @param contentType the declared content type, or null
     * @return the document
     * @throws UpstreamFormatException when the body is too large, not a recognized feed, or not well-formed
     */
    public static FeedDocument parse(byte[] body, String contentType) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("The feed is empty", "");
        }
        if (body.length > MAX_BYTES) {
            throw new UpstreamFormatException("The feed exceeds " + MAX_BYTES + " bytes", "");
        }
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).split(";")[0].strip();
        boolean json;
        if (type.equals("application/feed+json") || type.equals("application/json")) {
            json = true;
        } else if (type.endsWith("/xml") || type.endsWith("+xml")) {
            json = false;
        } else {
            json = firstByte(body) == '{';
        }
        return json ? jsonFeed(body) : xml(body);
    }

    private static int firstByte(byte[] body) {
        int i = 0;
        if (body.length >= 3 && (body[0] & 0xFF) == 0xEF && (body[1] & 0xFF) == 0xBB && (body[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        for (; i < body.length; i++) {
            if (!Character.isWhitespace(body[i])) {
                return body[i];
            }
        }
        return -1;
    }

    // ---- XML ----

    private static FeedDocument xml(byte[] body) {
        XmlTree.Node root = XmlTree.read(body);
        if (root.local().equals("rss")) {
            return rss2(root);
        }
        if (root.is(RDF_NS, "RDF")) {
            return rss1(root);
        }
        if (root.is(ATOM_NS, "feed")) {
            return atom(root);
        }
        throw new UpstreamFormatException("Not a feed: the root element is " + root.local(), "");
    }

    private static FeedDocument rss2(XmlTree.Node root) {
        XmlTree.Node channel = root.child("", "channel");
        if (channel == null) {
            throw new UpstreamFormatException("RSS feed has no channel", "");
        }
        List<String> warnings = new ArrayList<>();
        String base = firstNonNull(root.attr("xml:base"), channel.attr("xml:base"));
        String feedLink = FeedText.link(channel.childText("", "link"), base);
        String linkBase = firstNonNull(base, feedLink);
        String language = channel.childText("", "language");
        String self = null;
        List<String> hubs = new ArrayList<>();
        for (XmlTree.Node link : channel.all(ATOM_NS, "link")) {
            String rel = link.attr("rel");
            String href = FeedText.link(link.attr("href"), linkBase);
            if ("self".equals(rel) && self == null) {
                self = href;
            } else if ("hub".equals(rel) && href != null) {
                hubs.add(href);
            }
        }
        List<FeedItem> items = new ArrayList<>();
        for (XmlTree.Node item : channel.all("", "item")) {
            String title = FeedText.plain(item.childText("", "title"), FeedText.MAX_TITLE_BYTES);
            String link = FeedText.link(item.childText("", "link"), linkBase);
            String summary = FeedText.plain(firstNonNull(item.childText("", "description"),
                    item.childText(CONTENT_NS, "encoded")), FeedText.MAX_SUMMARY_BYTES);
            Instant published = FeedDates.parse(firstNonNull(item.childText("", "pubDate"),
                    item.childText(DC_NS, "date")), warnings);
            Instant updated = FeedDates.parse(item.childText(ATOM_NS, "updated"), warnings);
            List<String> authors = texts(item, "", "author", DC_NS, "creator");
            List<String> categories = texts(item, "", "category", DC_NS, "subject");
            List<FeedItem.Enclosure> enclosures = new ArrayList<>();
            for (XmlTree.Node e : item.all("", "enclosure")) {
                enclosure(e.attr("url"), e.attr("type"), e.attr("length"), linkBase, enclosures);
            }
            String guid = item.childText("", "guid");
            items.add(new FeedItem(id(guid, link, title), title, summary, link, published, updated, authors,
                    categories, enclosures, language, FeedGeometry.of(item, warnings)));
        }
        return new FeedDocument(FeedDocument.RSS2, FeedText.plain(channel.childText("", "title"),
                FeedText.MAX_TITLE_BYTES), feedLink, self, hubs, language, items, warnings);
    }

    private static FeedDocument rss1(XmlTree.Node root) {
        List<String> warnings = new ArrayList<>();
        XmlTree.Node channel = root.child(RSS1_NS, "channel");
        String base = root.attr("xml:base");
        String feedLink = channel == null ? null : FeedText.link(channel.childText(RSS1_NS, "link"), base);
        String linkBase = firstNonNull(base, feedLink);
        String language = channel == null ? null : channel.childText(DC_NS, "language");
        String self = null;
        List<String> hubs = new ArrayList<>();
        if (channel != null) {
            for (XmlTree.Node link : channel.all(ATOM_NS, "link")) {
                String href = FeedText.link(link.attr("href"), linkBase);
                if ("self".equals(link.attr("rel")) && self == null) {
                    self = href;
                } else if ("hub".equals(link.attr("rel")) && href != null) {
                    hubs.add(href);
                }
            }
        }
        List<FeedItem> items = new ArrayList<>();
        for (XmlTree.Node item : root.all(RSS1_NS, "item")) {
            String title = FeedText.plain(item.childText(RSS1_NS, "title"), FeedText.MAX_TITLE_BYTES);
            String link = FeedText.link(item.childText(RSS1_NS, "link"), linkBase);
            String summary = FeedText.plain(firstNonNull(item.childText(RSS1_NS, "description"),
                    item.childText(CONTENT_NS, "encoded")), FeedText.MAX_SUMMARY_BYTES);
            Instant published = FeedDates.parse(item.childText(DC_NS, "date"), warnings);
            String about = item.attr("about");
            items.add(new FeedItem(id(about, link, title), title, summary, link, published, null,
                    texts(item, DC_NS, "creator", null, null), texts(item, DC_NS, "subject", null, null), List.of(),
                    firstNonNull(item.childText(DC_NS, "language"), language), FeedGeometry.of(item, warnings)));
        }
        String title = channel == null ? null : FeedText.plain(channel.childText(RSS1_NS, "title"),
                FeedText.MAX_TITLE_BYTES);
        return new FeedDocument(FeedDocument.RSS1, title, feedLink, self, hubs, language, items, warnings);
    }

    private static FeedDocument atom(XmlTree.Node feed) {
        List<String> warnings = new ArrayList<>();
        String base = feed.attr("xml:base");
        String language = feed.attr("xml:lang");
        String self = null;
        String alternate = null;
        List<String> hubs = new ArrayList<>();
        for (XmlTree.Node link : feed.all(ATOM_NS, "link")) {
            String rel = link.attr("rel") == null ? "alternate" : link.attr("rel");
            String href = FeedText.link(link.attr("href"), base);
            if (rel.equals("self") && self == null) {
                self = href;
            } else if (rel.equals("hub") && href != null) {
                hubs.add(href);
            } else if (rel.equals("alternate") && alternate == null) {
                alternate = href;
            }
        }
        String linkBase = firstNonNull(base, firstNonNull(alternate, self));
        List<FeedItem> items = new ArrayList<>();
        for (XmlTree.Node entry : feed.all(ATOM_NS, "entry")) {
            String entryBase = firstNonNull(FeedText.link(entry.attr("xml:base"), linkBase), linkBase);
            String title = FeedText.plain(atomText(entry.child(ATOM_NS, "title")), FeedText.MAX_TITLE_BYTES);
            String link = null;
            List<FeedItem.Enclosure> enclosures = new ArrayList<>();
            for (XmlTree.Node l : entry.all(ATOM_NS, "link")) {
                String rel = l.attr("rel") == null ? "alternate" : l.attr("rel");
                if (rel.equals("alternate") && link == null) {
                    link = FeedText.link(l.attr("href"), entryBase);
                } else if (rel.equals("enclosure")) {
                    enclosure(l.attr("href"), l.attr("type"), l.attr("length"), entryBase, enclosures);
                }
            }
            String summary = FeedText.plain(firstNonNull(atomText(entry.child(ATOM_NS, "summary")),
                    atomText(entry.child(ATOM_NS, "content"))), FeedText.MAX_SUMMARY_BYTES);
            List<String> authors = new ArrayList<>();
            for (XmlTree.Node author : entry.all(ATOM_NS, "author")) {
                add(authors, author.childText(ATOM_NS, "name"));
            }
            List<String> categories = new ArrayList<>();
            for (XmlTree.Node category : entry.all(ATOM_NS, "category")) {
                add(categories, firstNonNull(category.attr("label"), category.attr("term")));
            }
            Instant published = FeedDates.parse(entry.childText(ATOM_NS, "published"), warnings);
            Instant updated = FeedDates.parse(entry.childText(ATOM_NS, "updated"), warnings);
            items.add(new FeedItem(id(entry.childText(ATOM_NS, "id"), link, title), title, summary, link, published,
                    updated, authors, categories, enclosures, firstNonNull(entry.attr("xml:lang"), language),
                    FeedGeometry.of(entry, warnings)));
        }
        return new FeedDocument(FeedDocument.ATOM, FeedText.plain(atomText(feed.child(ATOM_NS, "title")),
                FeedText.MAX_TITLE_BYTES), alternate, self, hubs, language, items, warnings);
    }

    /** An Atom text construct's text: {@code text} and {@code html} as written, {@code xhtml} as its text nodes. */
    private static String atomText(XmlTree.Node node) {
        if (node == null) {
            return null;
        }
        String type = node.attr("type");
        String text = "xhtml".equals(type) ? node.innerMarkup() : node.text().toString();
        return text.isBlank() ? null : text;
    }

    // ---- JSON Feed ----

    private static FeedDocument jsonFeed(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("The feed is not valid JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject() || !root.has("version")
                || !text(root.get("version")).startsWith("https://jsonfeed.org/version/")) {
            throw new UpstreamFormatException("Not a JSON Feed: no jsonfeed.org version", "");
        }
        List<String> warnings = new ArrayList<>();
        String home = FeedText.link(text(root.get("home_page_url")), null);
        String self = FeedText.link(text(root.get("feed_url")), null);
        String language = blankToNull(text(root.get("language")));
        List<String> hubs = new ArrayList<>();
        JsonNode hubList = root.get("hubs");
        if (hubList != null && hubList.isArray()) {
            for (JsonNode hub : hubList) {
                String url = FeedText.link(text(hub.get("url")), null);
                String hubType = text(hub.get("type")).toLowerCase(Locale.ROOT);
                if (url != null && (hubType.isEmpty() || hubType.equals("websub") || hubType.equals("pubsubhubbub"))) {
                    hubs.add(url);
                }
            }
        }
        String linkBase = firstNonNull(self, home);
        List<FeedItem> items = new ArrayList<>();
        JsonNode list = root.get("items");
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("JSON Feed has no items array", "");
        }
        for (JsonNode item : list) {
            if (!item.isObject()) {
                continue;
            }
            String title = FeedText.plain(blankToNull(text(item.get("title"))), FeedText.MAX_TITLE_BYTES);
            String link = FeedText.link(firstNonNull(blankToNull(text(item.get("url"))),
                    blankToNull(text(item.get("external_url")))), linkBase);
            String summary = FeedText.plain(firstNonNull(blankToNull(text(item.get("summary"))),
                    firstNonNull(blankToNull(text(item.get("content_text"))),
                            blankToNull(text(item.get("content_html"))))), FeedText.MAX_SUMMARY_BYTES);
            List<String> authors = new ArrayList<>();
            JsonNode authorList = item.get("authors");
            if (authorList != null && authorList.isArray()) {
                for (JsonNode a : authorList) {
                    add(authors, text(a.get("name")));
                }
            }
            JsonNode author = item.get("author");
            if (author != null && author.isObject()) {
                add(authors, text(author.get("name")));
            }
            List<String> categories = new ArrayList<>();
            JsonNode tags = item.get("tags");
            if (tags != null && tags.isArray()) {
                for (JsonNode t : tags) {
                    add(categories, text(t));
                }
            }
            List<FeedItem.Enclosure> enclosures = new ArrayList<>();
            JsonNode attachments = item.get("attachments");
            if (attachments != null && attachments.isArray()) {
                for (JsonNode a : attachments) {
                    enclosure(text(a.get("url")), blankToNull(text(a.get("mime_type"))),
                            blankToNull(text(a.get("size_in_bytes"))), linkBase, enclosures);
                }
            }
            Geometry geometry = null;
            JsonNode geo = item.get("_geo");
            if (geo != null && geo.isObject()) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> map = JSON.treeToValue(geo, java.util.Map.class);
                    geometry = Geo.fromGeoJson(map);
                } catch (UpstreamFormatException | JacksonException e) {
                    if (warnings.size() < 50) {
                        warnings.add("geometry left out: " + e.getMessage());
                    }
                }
            }
            items.add(new FeedItem(id(blankToNull(text(item.get("id"))), link, title), title, summary, link,
                    FeedDates.parse(blankToNull(text(item.get("date_published"))), warnings),
                    FeedDates.parse(blankToNull(text(item.get("date_modified"))), warnings), authors, categories,
                    enclosures, firstNonNull(blankToNull(text(item.get("language"))), language), geometry));
        }
        return new FeedDocument(FeedDocument.JSON_FEED, FeedText.plain(blankToNull(text(root.get("title"))),
                FeedText.MAX_TITLE_BYTES), home, self, hubs, language, items, warnings);
    }

    // ---- shared ----

    /**
     * The item id: {@code candidate} when present, else the first 16 hex characters of SHA-256 of
     * {@code link + title}.
     */
    static String id(String candidate, String link, String title) {
        if (candidate != null && !candidate.isBlank()) {
            return candidate.strip();
        }
        String input = (link == null ? "" : link) + (title == null ? "" : title);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void enclosure(String url, String type, String length, String base,
            List<FeedItem.Enclosure> out) {
        String href = FeedText.link(url, base);
        if (href == null) {
            return;
        }
        Long size = null;
        if (length != null) {
            try {
                long parsed = Long.parseLong(length.strip());
                size = parsed >= 0 ? parsed : null;
            } catch (NumberFormatException ignored) {
                // a non-numeric length is left out
            }
        }
        out.add(new FeedItem.Enclosure(href, type == null ? null : FeedText.cut(type.strip(), 128), size));
    }

    private static List<String> texts(XmlTree.Node node, String ns1, String name1, String ns2, String name2) {
        Set<String> out = new LinkedHashSet<>();
        for (XmlTree.Node c : node.all(ns1, name1)) {
            String t = FeedText.plain(c.text().toString(), FeedText.MAX_SHORT_BYTES);
            if (t != null) {
                out.add(t);
            }
        }
        if (ns2 != null) {
            for (XmlTree.Node c : node.all(ns2, name2)) {
                String t = FeedText.plain(c.text().toString(), FeedText.MAX_SHORT_BYTES);
                if (t != null) {
                    out.add(t);
                }
            }
        }
        return new ArrayList<>(out);
    }

    private static void add(List<String> out, String value) {
        String t = FeedText.plain(value, FeedText.MAX_SHORT_BYTES);
        if (t != null && !out.contains(t)) {
            out.add(t);
        }
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return "";
        }
        return node.isString() ? node.stringValue() : node.asString();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}

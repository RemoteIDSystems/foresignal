package ai.foresignal.source.formats.news;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Article URL canonicalization (prompt 133): the identity of a news article is the SHA-256 of its canonical URL, so the
 * same article seen by GDELT and by an outlet feed, or with different tracking parameters, is one record.
 *
 * <p>The canonical form keeps the scheme and path, lower-cases the scheme and host, drops a default port, the user
 * information, the fragment and every tracking parameter ({@link #TRACKING} and any {@code utm_*} parameter), and keeps
 * the remaining query parameters in their original order. Pure.
 */
public final class ArticleUrls {

    /** Query parameters that only track a click and never identify an article. */
    public static final Set<String> TRACKING = Set.of("fbclid", "gclid", "dclid", "gbraid", "wbraid", "msclkid",
            "mc_cid", "mc_eid", "igshid", "yclid", "_ga", "_gl", "ref", "ref_src", "ref_url", "cmpid", "smid", "ocid",
            "at_medium", "at_campaign", "at_custom1", "at_custom2", "at_custom3", "at_custom4", "spm", "share",
            "s_cid", "icid", "mbid", "rss", "ito", "taid", "cid", "sr_share", "xtor");

    private ArticleUrls() {}

    /**
     * The canonical form of {@code url}, or null when it is not an absolute http(s) URL with a host.
     *
     * @param url the article URL as the upstream gave it
     */
    public static String canonical(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        URI uri;
        try {
            uri = new URI(url.strip().replace(" ", "%20"));
        } catch (URISyntaxException e) {
            return null;
        }
        String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme) || uri.getHost() == null || uri.getHost().isBlank()) {
            return null;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        int port = uri.getPort();
        boolean defaultPort = port < 0 || ("http".equals(scheme) && port == 80) || ("https".equals(scheme)
                && port == 443);
        StringBuilder out = new StringBuilder(scheme).append("://").append(host);
        if (!defaultPort) {
            out.append(':').append(port);
        }
        String path = uri.getRawPath();
        out.append(path == null || path.isEmpty() ? "/" : path);
        String query = uri.getRawQuery();
        if (query != null && !query.isEmpty()) {
            List<String> kept = new ArrayList<>();
            for (String part : query.split("&")) {
                if (part.isEmpty()) {
                    continue;
                }
                int eq = part.indexOf('=');
                String name = (eq < 0 ? part : part.substring(0, eq)).toLowerCase(Locale.ROOT);
                if (name.startsWith("utm_") || TRACKING.contains(name)) {
                    continue;
                }
                kept.add(part);
            }
            if (!kept.isEmpty()) {
                out.append('?').append(String.join("&", kept));
            }
        }
        return out.toString();
    }

    /**
     * The external id of an article: the lower-case hex SHA-256 of its canonical URL, or null when the URL is not
     * usable.
     */
    public static String externalId(String url) {
        String canonical = canonical(url);
        return canonical == null ? null : sha256(canonical);
    }

    /**
     * The outlet domain of a URL: its lower-case host without a leading {@code www.}, or null.
     *
     * @param url an absolute URL
     */
    public static String domain(String url) {
        String canonical = canonical(url);
        if (canonical == null) {
            return null;
        }
        return normalizeDomain(URI.create(canonical).getHost());
    }

    /** A host or domain lower-cased, without a trailing dot or a leading {@code www.}; null for blank. */
    public static String normalizeDomain(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        String h = host.strip().toLowerCase(Locale.ROOT);
        if (h.endsWith(".")) {
            h = h.substring(0, h.length() - 1);
        }
        return h.startsWith("www.") ? h.substring(4) : h;
    }

    /** The lower-case hex SHA-256 of {@code text}. */
    public static String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}

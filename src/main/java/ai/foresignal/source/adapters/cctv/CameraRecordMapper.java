package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.spi.NormalizedRecord;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;

/**
 * The one mapping from {@link CameraEntry} to a {@code camera} record, shared by every pack (prompt 072). Pure.
 *
 * <ul>
 *   <li><strong>Identity and place:</strong> {@code externalId} = the upstream camera id; geometry
 *       {@code Point(lon, lat)}. A camera without coordinates ({@link PackContext#NO_COORDINATES}) or outside the
 *       pack's bounding box ({@link PackContext#OUT_OF_BOUNDS}) is skipped and counted; a repeated id keeps the first
 *       entry ({@link PackContext#DUPLICATE}).
 *   <li><strong>Payload</strong> (allowlist, nulls omitted, no counters, no upstream timestamps): {@code name,
 *       provider, region, road, direction, headingDeg, status, imageUrl, videoUrl, refreshSeconds, frameAccess,
 *       frameInsecure}. {@code status} is {@code active} or {@code offline}; {@code headingDeg} only when the parser
 *       read a published direction.
 *   <li><strong>Frame URLs:</strong> each must parse as an absolute {@code http}/{@code https} URL without user info
 *       that starts with one of the pack's allowed frame origins; otherwise the URL is dropped and the camera kept.
 *       Accepted URLs are written in canonical form (lowercase scheme and host, no default port, no fragment).
 *   <li><strong>{@code frameAccess}</strong> is the {@link CctvFrameHosts} hint of the kept image URL's host (else
 *       the kept video URL's); absent when no frame URL survives. {@code frameInsecure: true} when that URL is
 *       {@code http:} (packs whose host also serves https upgrade in the parser, as Calgary does).
 * </ul>
 */
public final class CameraRecordMapper {

    /** The records' kind. */
    public static final String KIND = "camera";

    /** {@code status} of a camera in service. */
    public static final String ACTIVE = "active";

    /** {@code status} of a camera the upstream marks out of service. */
    public static final String OFFLINE = "offline";

    /** The payload keys, in order. */
    public static final List<String> PAYLOAD_KEYS = List.of("name", "provider", "region", "road", "direction",
            "headingDeg", "status", "imageUrl", "videoUrl", "refreshSeconds", "frameAccess", "frameInsecure");

    /**
     * The records of one fetch, and how many cameras the {@code bbox} parameter left out.
     *
     * @param records the camera records, in upstream order
     * @param outsideBbox cameras outside the operator's {@code bbox}
     */
    public record Mapped(List<NormalizedRecord> records, int outsideBbox) {}

    private CameraRecordMapper() {}

    /**
     * Maps a fetch's entries.
     *
     * @param pack the pack
     * @param entries the parser's entries
     * @param bbox the operator's {@code bbox} parameter, or null for the whole pack
     * @param pc where skipped cameras are counted
     */
    public static Mapped mapAll(CctvPack pack, List<CameraEntry> entries, Envelope bbox, PackContext pc) {
        List<NormalizedRecord> out = new ArrayList<>(entries.size());
        Set<String> seen = new HashSet<>();
        int outside = 0;
        for (CameraEntry entry : entries) {
            NormalizedRecord record = map(pack, entry, pc);
            if (record == null) {
                continue;
            }
            if (!seen.add(record.externalId())) {
                pc.skip(PackContext.DUPLICATE);
                continue;
            }
            if (bbox != null && !bbox.isNull() && !bbox.contains(entry.lon(), entry.lat())) {
                outside++;
                continue;
            }
            out.add(record);
        }
        return new Mapped(out, outside);
    }

    /**
     * Maps one entry.
     *
     * @return the record, or null when the camera is skipped (and counted in {@code pc})
     */
    public static NormalizedRecord map(CctvPack pack, CameraEntry entry, PackContext pc) {
        if (entry.id() == null || entry.id().isBlank()) {
            pc.skip(PackContext.MALFORMED);
            return null;
        }
        if (entry.lat() == null || entry.lon() == null) {
            pc.skip(PackContext.NO_COORDINATES);
            return null;
        }
        if (pack.bounds() != null && !pack.bounds().contains(entry.lat(), entry.lon())) {
            pc.skip(PackContext.OUT_OF_BOUNDS);
            return null;
        }
        String image = frameUrl(entry.imageUrl(), pack.allowedFrameOrigins());
        String video = frameUrl(entry.videoUrl(), pack.allowedFrameOrigins());
        String frame = image != null ? image : video;
        String access = null;
        Boolean insecure = null;
        if (frame != null) {
            URI uri = URI.create(frame);
            access = CctvFrameHosts.access(uri.getHost());
            insecure = "http".equals(uri.getScheme()) ? Boolean.TRUE : null;
        }
        Double heading = entry.headingDeg() == null || !Double.isFinite(entry.headingDeg()) ? null
                : ((entry.headingDeg() % 360) + 360) % 360;
        return NormalizedRecord.builder(entry.id().strip(), KIND)
                .point(entry.lon(), entry.lat())
                .put("name", blank(entry.name()))
                .put("provider", blank(entry.provider()) != null ? entry.provider().strip() : pack.provider())
                .put("region", blank(entry.region()))
                .put("road", blank(entry.road()))
                .put("direction", blank(entry.direction()))
                .put("headingDeg", heading)
                .put("status", entry.active() ? ACTIVE : OFFLINE)
                .put("imageUrl", image)
                .put("videoUrl", video)
                .put("refreshSeconds", entry.refreshSeconds())
                .put("frameAccess", access)
                .put("frameInsecure", insecure)
                .build();
    }

    /**
     * {@code raw} in canonical form when it is an absolute {@code http}/{@code https} URL without user info that
     * starts with one of {@code allowedPrefixes}; otherwise null.
     */
    public static String frameUrl(String raw, List<String> allowedPrefixes) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        URI uri;
        try {
            uri = new URI(raw.strip());
        } catch (URISyntaxException e) {
            return null;
        }
        String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!uri.isAbsolute() || uri.isOpaque() || uri.getHost() == null || uri.getRawUserInfo() != null
                || !("https".equals(scheme) || "http".equals(scheme))) {
            return null;
        }
        int port = uri.getPort();
        boolean defaultPort = port == -1 || ("https".equals(scheme) && port == 443) || ("http".equals(scheme)
                && port == 80);
        String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String canonical = scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + (defaultPort ? "" : ":" + port)
                + path + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        for (String prefix : allowedPrefixes) {
            if (canonical.startsWith(prefix)) {
                return canonical;
            }
        }
        return null;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}

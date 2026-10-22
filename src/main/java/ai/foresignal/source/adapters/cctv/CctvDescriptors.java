package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.Set;

/**
 * The descriptor of a {@code cctv-*} source (prompt 072).
 *
 * <ul>
 *   <li><strong>Schedule:</strong> POLLING, the pack's interval (15 min by default, at least 10 min; TfL 60 min, the
 *       bundled packs 24 h) and budget (2/min, 200/day by default); redirects refused.
 *   <li><strong>History:</strong> STANDARD, policy ALL with no keyframe, no record TTL, {@code observedAtMode}
 *       RECEIVED (catalogs carry no per-camera update time), {@code ttlAnchor} RECEIVED; no significant or volatile
 *       fields, so every payload key is significant; retention 90 d (the {@code reference} class).
 *   <li><strong>Display and search:</strong> title {@code name}; search {@code name, road, region}; tiles
 *       {@code name, direction, status, frameAccess}; {@code zeroResultsNormal} false.
 *   <li><strong>Parameter:</strong> optional {@code bbox} keeps only the cameras inside it.
 * </ul>
 *
 * <p>License, attribution and terms come from the pack; a pack's source starts enabled unless it needs a credential
 * (the seeding rule of prompt 145).
 */
public final class CctvDescriptors {

    /** Raw retention: the {@code reference} class (Conventions), 90 days. */
    public static final Duration RETENTION = Duration.ofDays(90);

    /** The fetch timeout. */
    public static final Duration FETCH_TIMEOUT = Duration.ofSeconds(60);

    /** The {@code bbox} parameter. */
    public static final String BBOX_PARAM = "bbox";

    private CctvDescriptors() {}

    /** The descriptor of {@code pack}'s source. */
    public static SourceDescriptor descriptor(CctvPack pack) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(pack.id())
                .name(pack.name())
                .description("Traffic-camera catalog of " + pack.provider() + ": one camera record per camera with"
                        + " its location, published direction, still-image or official stream URL and a frameAccess"
                        + " hint. Frames are never fetched or stored by Foresignal; clients load them from the"
                        + " upstream host. A camera missing from two consecutive complete fetches is tombstoned.")
                .kind(CameraRecordMapper.KIND)
                .homepage(pack.homepage())
                .attribution(pack.attribution())
                .licence(pack.license())
                .termsUrl(pack.termsUrl())
                .mode(Mode.POLLING)
                .interval(pack.minInterval(), pack.defaultInterval())
                .defaultBudget(pack.budget())
                .maxResponseBytes(pack.maxResponseBytes())
                .fetchTimeout(FETCH_TIMEOUT)
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .defaultRetention(RETENTION)
                .kindRetention(CameraRecordMapper.KIND, RETENTION)
                .zeroResultsNormal(false)
                .titleField("name")
                .searchFields("name", "road", "region")
                .tileProperties("name", "direction", "status", "frameAccess")
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX, "Keep only the cameras inside this box"
                        + " (minLon,minLat,maxLon,maxLat); the whole pack when absent", null))
                .kindDisplay(CameraRecordMapper.KIND, new KindDisplay("{name}", "camera", "#5C7CFA", null));
        if (pack.credential() != null) {
            b.credential(pack.credential());
        }
        text(b, "name", "Name", "The camera's name as published");
        text(b, "provider", "Provider", "The operator, or the image credit the upstream publishes for the camera");
        text(b, "region", "Region", "The published region, district, county or city quadrant");
        text(b, "road", "Road", "The published road");
        text(b, "direction", "Direction", "The published direction text, verbatim (for Caltrans the direction of"
                + " travel of the road the camera is on)");
        b.fieldSpec("headingDeg", new FieldSpec(FieldType.NUMBER, "deg", "Heading", "The camera's facing, clockwise"
                + " from north; only when the upstream publishes a compass direction for the camera", null, Set.of()));
        text(b, "status", "Status", "active, or offline when the upstream marks the camera out of service");
        text(b, "imageUrl", "Image URL", "The upstream still-image URL; clients load it directly, see frameAccess");
        text(b, "videoUrl", "Video URL", "The upstream's official live-stream (HLS) URL");
        b.fieldSpec("refreshSeconds", new FieldSpec(FieldType.INTEGER, "s", "Refresh interval", "How often the"
                + " upstream says the still is updated", null, Set.of()));
        text(b, "frameAccess", "Frame access", "direct (any client), browser (only a real browser <img>) or key"
                + " (the frame host needs a key)");
        b.fieldSpec("frameInsecure", new FieldSpec(FieldType.BOOLEAN, null, "Insecure frame URL", "Present and true"
                + " when the frame URL is http: with no https equivalent", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}

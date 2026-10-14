package ai.foresignal.source.adapters.cap;

import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapGeometry;
import ai.foresignal.source.formats.cap.CapRecordMapper;
import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * Maps one parsed CAP message to a Foresignal {@code cap-alert} record (prompt 089), shared by the CAP feed
 * family and the MeteoAlarm family.
 *
 * <p><strong>Time base.</strong> {@code observedAt = sent}; {@code eventTime = onset}; {@code validFrom =
 * onset ?? effective}; {@code validUntil = expires}. An inverted range drops {@code validFrom} rather than
 * losing the alert (as prompt 067 does).
 *
 * <p><strong>Threads.</strong> The caller supplies the record's {@code externalId}, resolved by the shared
 * update chain ({@link ai.foresignal.source.formats.cap.CapThreadResolver}). A {@code msgType = Cancel}
 * becomes a tombstone of that thread; every other message versions it.
 *
 * <p><strong>Info choice.</strong> The first {@code info} block matching the source's languages (exactly,
 * then by subtag, in order) is read; the headlines of the other languages go to the payload's
 * {@code headlines} map.
 */
public final class CapAlertRecords {

    /** The record kind of every CAP source. */
    public static final String KIND = "cap-alert";

    /** The UTF-8 cap of {@code headline} and {@code description}. */
    public static final int DESCRIPTION_BYTES = 4 * 1024;

    /** The UTF-8 cap of {@code instruction}. */
    public static final int INSTRUCTION_BYTES = 2 * 1024;

    /** The embedded (untrusted) fields. */
    public static final List<String> EMBED_FIELDS = List.of("headline", "description");

    private CapAlertRecords() {}

    /**
     * The record of {@code alert}, or null when it is skipped.
     *
     * @param alert the parsed message
     * @param threadKey the record's {@code externalId}
     * @param languages preferred {@code info} languages, best first
     * @param includeTests whether a non-{@code Actual} status is kept
     * @return the record, a tombstone for a {@code Cancel}, or null when skipped
     */
    public static NormalizedRecord map(CapAlert alert, String threadKey, List<String> languages, boolean includeTests) {
        if (!"Actual".equals(alert.status()) && !includeTests) {
            return null;
        }
        Instant sent = alert.sent() == null ? null : alert.sent().toInstant();
        if ("Cancel".equals(alert.msgType())) {
            return NormalizedRecord.tombstone(threadKey, KIND, sent);
        }
        CapAlert.Info info = CapRecordMapper.chooseInfo(alert, languages);
        Instant onset = instant(info == null ? null : info.onset());
        Instant effective = instant(info == null ? null : info.effective());
        Instant expires = instant(info == null ? null : info.expires());
        Instant validFrom = onset != null ? onset : effective;
        if (validFrom != null && expires != null && validFrom.isAfter(expires)) {
            validFrom = null;
        }
        Geometry geometry = CapGeometry.of(info);
        boolean geocodeOnly = geometry == null && hasGeocodes(info);

        NormalizedRecord.Builder b = NormalizedRecord.builder(threadKey, KIND)
                .observedAt(sent)
                .eventTime(onset)
                .validFrom(validFrom)
                .validUntil(expires)
                .geometry(geometry);
        b.put("identifier", alert.identifier());
        b.put("sender", alert.sender());
        b.put("status", alert.status());
        b.put("msgType", alert.msgType());
        b.put("scope", alert.scope());
        if (info != null) {
            b.put("category", info.categories().isEmpty() ? null : info.categories());
            b.put("event", CapRecordMapper.truncate(info.event(), DESCRIPTION_BYTES));
            b.put("urgency", info.urgency());
            b.put("severity", info.severity());
            b.put("certainty", info.certainty());
            b.put("headline", CapRecordMapper.truncate(info.headline(), DESCRIPTION_BYTES));
            b.put("description", CapRecordMapper.truncate(info.description(), DESCRIPTION_BYTES));
            b.put("instruction", CapRecordMapper.truncate(info.instruction(), INSTRUCTION_BYTES));
            b.put("web", CapRecordMapper.truncate(info.web(), DESCRIPTION_BYTES));
            b.put("areaDesc", CapRecordMapper.truncate(areaDesc(info), DESCRIPTION_BYTES));
            b.put("geocodes", geocodes(info));
            b.put("language", info.language());
        }
        Map<String, Object> headlines = headlines(alert);
        if (!headlines.isEmpty()) {
            b.put("headlines", headlines);
        }
        if (geocodeOnly) {
            b.put("geometryFromGeocode", false);
        }
        return b.build();
    }

    private static Map<String, Object> headlines(CapAlert alert) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (CapAlert.Info info : alert.infos()) {
            String language = info.language();
            if (language != null && info.headline() != null && !info.headline().isBlank()) {
                out.putIfAbsent(language, CapRecordMapper.truncate(info.headline(), DESCRIPTION_BYTES));
            }
        }
        return out;
    }

    private static boolean hasGeocodes(CapAlert.Info info) {
        if (info == null) {
            return false;
        }
        return info.areas().stream().anyMatch(area -> !area.geocodes().isEmpty());
    }

    private static String areaDesc(CapAlert.Info info) {
        Set<String> parts = new LinkedHashSet<>();
        for (CapAlert.Area area : info.areas()) {
            if (area.areaDesc() != null && !area.areaDesc().isBlank()) {
                parts.add(area.areaDesc().strip());
            }
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }

    private static Map<String, Object> geocodes(CapAlert.Info info) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (CapAlert.Area area : info.areas()) {
            for (CapAlert.NamedValue code : area.geocodes()) {
                if (code.value() != null) {
                    List<String> values = out.computeIfAbsent(code.valueName(), k -> new ArrayList<>());
                    if (!values.contains(code.value())) {
                        values.add(code.value());
                    }
                }
            }
        }
        return out.isEmpty() ? null : new LinkedHashMap<>(out);
    }

    private static Instant instant(java.time.OffsetDateTime time) {
        return time == null ? null : time.toInstant();
    }

    /** Exposed for tests that check the exact byte cap. */
    static int utf8Length(String value) {
        return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
    }
}

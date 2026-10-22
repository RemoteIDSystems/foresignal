package ai.foresignal.source.formats.cap;

import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * Maps a parsed CAP alert to one Foresignal record (prompt 067), the alert time mapping shared by every
 * CAP source.
 *
 * <p><strong>Time base.</strong> {@code observedAt = sent}; {@code eventTime = onset} when present;
 * {@code validFrom = onset ?? effective} (null when neither); {@code validUntil = expires}, explicit and
 * upstream-asserted (Core-Data-Model §3 rule 9). When {@code validFrom} would fall after
 * {@code validUntil}, which the normalizer rejects as an inverted range, {@code validFrom} is left out
 * rather than losing the alert.
 *
 * <p><strong>Threads and state.</strong> {@code externalId} is the thread key ({@link CapThreadResolver}).
 * {@code state} is {@code cancelled} when {@code msgType = Cancel} or the profile's cancellation rule
 * matches, {@code updated} for {@code msgType = Update}, otherwise {@code active}. <strong>A cancellation
 * is a version, not a tombstone</strong>: the record keeps its thread id with {@code state: cancelled} and
 * {@code validUntil = info.expires ?? sent}, so it expires on its own and a rule on {@code state} sees the
 * change as SIGNIFICANT. Absence from a feed never closes an alert; nothing here produces a tombstone.
 *
 * <p><strong>Payload</strong>: {@code identifier, sender, senderName, sent, msgType, status, scope, state,
 * event, category, urgency, severity, certainty, headline, description, instruction} (each text capped at
 * the profile's limit, 4 KB by default, ending in {@code …} when cut), {@code web, areaDesc, geocodes}
 * ({@code {valueName: [values]}}), {@code parameters} (allowlisted names only, {@code {valueName:
 * [values]}}), {@code onset, effective, expires} (ISO instants), {@code language} and {@code references}
 * (the number of referenced messages). {@code headline}, {@code description} and {@code instruction} are
 * the embedded fields ({@link #EMBED_FIELDS}); they are untrusted upstream text under prompt 028's rules.
 *
 * <p><strong>Info choice.</strong> With several {@code info} blocks, the first whose language matches the
 * profile's list (exactly, then by primary subtag, in list order) is read; otherwise the first block.
 */
public final class CapRecordMapper {

    /** {@code state} of a live alert. */
    public static final String STATE_ACTIVE = "active";

    /** {@code state} after an {@code Update}. */
    public static final String STATE_UPDATED = "updated";

    /** {@code state} after a cancellation. */
    public static final String STATE_CANCELED = "cancelled";

    /** The embedded fields of every CAP source. */
    public static final List<String> EMBED_FIELDS = List.of("headline", "description", "instruction");

    private CapRecordMapper() {}

    /** {@link #toRecord(CapAlert, String, CapMappingProfile)} with the message's own {@code {sender}:{identifier}}. */
    public static NormalizedRecord toRecord(CapAlert alert, CapMappingProfile profile) {
        return toRecord(alert, alert.ownKey(), profile);
    }

    /**
     * The record of {@code alert} in thread {@code threadKey}.
     *
     * @param alert the parsed message
     * @param threadKey the record's {@code externalId}
     * @param profile the source's mapping profile
     * @return the record, never a tombstone
     */
    public static NormalizedRecord toRecord(CapAlert alert, String threadKey, CapMappingProfile profile) {
        CapAlert.Info info = chooseInfo(alert, profile.languages());
        String state = state(alert, info, profile);
        Instant sent = alert.sent().toInstant();
        Instant onset = instant(info == null ? null : info.onset());
        Instant effective = instant(info == null ? null : info.effective());
        Instant expires = instant(info == null ? null : info.expires());
        Instant validUntil = STATE_CANCELED.equals(state) ? (expires != null ? expires : sent) : expires;
        Instant validFrom = onset != null ? onset : effective;
        if (validFrom != null && validUntil != null && validFrom.isAfter(validUntil)) {
            validFrom = null;
        }
        Geometry geometry = CapGeometry.of(info);

        NormalizedRecord.Builder b = NormalizedRecord.builder(threadKey, profile.kind())
                .observedAt(sent)
                .eventTime(onset)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .geometry(geometry);
        int cap = profile.textMaxBytes();
        b.put("identifier", alert.identifier());
        b.put("sender", alert.sender());
        b.put("sent", sent.toString());
        b.put("msgType", alert.msgType());
        b.put("status", alert.status());
        b.put("scope", alert.scope());
        b.put("state", state);
        b.put("references", alert.references().size());
        if (info != null) {
            b.put("senderName", truncate(info.senderName(), cap));
            b.put("event", truncate(info.event(), cap));
            b.put("category", info.categories().isEmpty() ? null : info.categories());
            b.put("urgency", info.urgency());
            b.put("severity", info.severity());
            b.put("certainty", info.certainty());
            b.put("headline", truncate(info.headline(), cap));
            b.put("description", truncate(info.description(), cap));
            b.put("instruction", truncate(info.instruction(), cap));
            b.put("web", truncate(info.web(), cap));
            b.put("areaDesc", truncate(areaDesc(info), cap));
            b.put("geocodes", geocodes(info));
            b.put("parameters", parameters(info, profile.parameterAllowlist()));
            b.put("onset", iso(onset));
            b.put("effective", iso(effective));
            b.put("expires", iso(expires));
            b.put("language", info.language());
        }
        if (profile.extender() != null) {
            profile.extender().extend(alert, info, geometry, b);
        }
        return b.build();
    }

    /**
     * The {@code state} of {@code alert}: {@code cancelled}, {@code updated} or {@code active}.
     *
     * @param info the chosen info block, or null
     */
    public static String state(CapAlert alert, CapAlert.Info info, CapMappingProfile profile) {
        if ("Cancel".equals(alert.msgType())
                || (profile.cancellation() != null && profile.cancellation().cancels(alert, info))) {
            return STATE_CANCELED;
        }
        return "Update".equals(alert.msgType()) ? STATE_UPDATED : STATE_ACTIVE;
    }

    /**
     * The info block to read: the first matching a preferred language (exact, case-insensitive, then by
     * primary subtag, in preference order), else the first block; null when there is none.
     */
    public static CapAlert.Info chooseInfo(CapAlert alert, List<String> languages) {
        if (alert.infos().isEmpty()) {
            return null;
        }
        for (String wanted : languages) {
            for (CapAlert.Info info : alert.infos()) {
                if (info.language() != null && info.language().equalsIgnoreCase(wanted)) {
                    return info;
                }
            }
        }
        for (String wanted : languages) {
            String primary = primary(wanted);
            for (CapAlert.Info info : alert.infos()) {
                if (info.language() != null && primary(info.language()).equals(primary)) {
                    return info;
                }
            }
        }
        return alert.firstInfo();
    }

    /**
     * Caps {@code value} at {@code maxBytes} UTF-8 bytes, ending a cut value with {@code …} and never
     * splitting a code point; null for a null or blank value.
     */
    public static String truncate(String value, int maxBytes) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String stripped = value.strip();
        if (stripped.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return stripped;
        }
        int budget = maxBytes - "…".getBytes(StandardCharsets.UTF_8).length;
        StringBuilder out = new StringBuilder();
        int used = 0;
        for (int i = 0; i < stripped.length(); ) {
            int cp = stripped.codePointAt(i);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (used + size > budget) {
                break;
            }
            out.appendCodePoint(cp);
            used += size;
            i += Character.charCount(cp);
        }
        return out.append('…').toString();
    }

    private static String primary(String language) {
        int dash = language.indexOf('-');
        return (dash < 0 ? language : language.substring(0, dash)).toLowerCase(Locale.ROOT);
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

    private static Map<String, Object> parameters(CapAlert.Info info, Set<String> allowlist) {
        if (allowlist.isEmpty()) {
            return null;
        }
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (CapAlert.NamedValue parameter : info.parameters()) {
            if (allowlist.contains(parameter.valueName()) && parameter.value() != null) {
                out.computeIfAbsent(parameter.valueName(), k -> new ArrayList<>())
                        .add(truncate(parameter.value(), 1024));
            }
        }
        return out.isEmpty() ? null : new LinkedHashMap<>(out);
    }

    private static Instant instant(OffsetDateTime time) {
        return time == null ? null : time.toInstant();
    }

    private static String iso(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}

/*
 * The decoding bounds (a cap on decoded entities, string fields refused past 256 characters, the whole record
 * dropped rather than truncated) are ported from God's Eye View, src/data/gtfsRealtime.js (GTFS_MAX_ENTITIES,
 * GTFS_MAX_STRING_CHARS, readBoundedString), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; decoding is MobilityData's gtfs-realtime-bindings (FeedMessage.parseFrom) on a body already capped
 * at 16 MB, so the entity cap is checked after parsing; an HTML or JSON 200 is refused by its first byte.
 */
package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.time.Instant;
import java.util.Map;

/**
 * Reading a GTFS-RT {@code FeedMessage} from an upstream response (prompt 070).
 *
 * <ul>
 *   <li>A truncated body (over {@link #MAX_RESPONSE_BYTES}) is refused: half a feed would read as vanished vehicles
 *       and ended alerts.
 *   <li>An HTML or JSON 200 (an error page, a login wall, CapMetro's JSON alerts) is detected by its first
 *       non-blank byte before parsing: {@code <}, <code>{</code> or {@code [} cannot start a valid FeedMessage
 *       (they are protobuf end-group and start-group tags).
 *   <li>A protobuf parse failure is an {@link UpstreamFormatException}.
 *   <li>A message with more than {@link #MAX_ENTITIES} entities is refused.
 * </ul>
 */
public final class GtfsRt {

    /** The response cap of every transit source. */
    public static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    /** At most this many entities per message (GEV {@code GTFS_MAX_ENTITIES}). */
    public static final int MAX_ENTITIES = 50_000;

    /** The longest accepted id, label or stop string (GEV {@code GTFS_MAX_STRING_CHARS}). */
    public static final int MAX_STRING_CHARS = 256;

    /** How many leading bytes the text sniffing looks at. */
    static final int SNIFF_BYTES = 64;

    private GtfsRt() {}

    /**
     * One conditional GET of {@code url} with {@code headers}, parsed.
     *
     * @param stateKey the {@code getConditional} state key
     * @return the message, or null when the upstream answered 304
     * @throws AdapterException when the request fails; {@link UpstreamFormatException} when the body is unusable, in
     *     which case the stored validators are forgotten so the next run refetches
     */
    public static FeedMessage fetch(FetchContext ctx, URI url, String stateKey, Map<String, String> headers)
            throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(url, stateKey, headers);
        if (response.notModified()) {
            return null;
        }
        try {
            return parse(response);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw e;
        }
    }

    /**
     * Parses a 200 response.
     *
     * @throws UpstreamFormatException when the body is truncated, HTML, JSON, not a FeedMessage or too large
     */
    public static FeedMessage parse(UpstreamResponse response) throws UpstreamFormatException {
        if (response.truncated()) {
            throw new UpstreamFormatException("The GTFS-RT feed is larger than " + MAX_RESPONSE_BYTES + " bytes", "");
        }
        return parse(response.body());
    }

    /**
     * Parses a whole body.
     *
     * @throws UpstreamFormatException when the body is empty, HTML, JSON, not a FeedMessage or too large
     */
    public static FeedMessage parse(byte[] body) throws UpstreamFormatException {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("The GTFS-RT feed is empty", "");
        }
        String text = textKind(body);
        if (text != null) {
            throw new UpstreamFormatException("The GTFS-RT feed answered " + text + " instead of protobuf", snippet(body));
        }
        FeedMessage message;
        try {
            message = FeedMessage.parseFrom(body);
        } catch (InvalidProtocolBufferException e) {
            throw new UpstreamFormatException("The body is not a GTFS-RT FeedMessage: " + e.getMessage(), "", e);
        }
        if (!message.hasHeader()) {
            throw new UpstreamFormatException("The GTFS-RT FeedMessage has no header", "");
        }
        if (message.getEntityCount() > MAX_ENTITIES) {
            throw new UpstreamFormatException("The GTFS-RT feed has " + message.getEntityCount() + " entities, more than "
                    + MAX_ENTITIES, "");
        }
        return message;
    }

    /**
     * {@code "HTML"} or {@code "JSON"} when the body is text of that kind, else null: the first byte that is not a
     * space, tab, CR or LF, within the first {@value #SNIFF_BYTES} bytes, is {@code <}, <code>{</code> or {@code [}.
     */
    static String textKind(byte[] body) {
        int limit = Math.min(body.length, SNIFF_BYTES);
        for (int i = 0; i < limit; i++) {
            byte b = body[i];
            if (b == ' ' || b == '\t' || b == '\r' || b == '\n') {
                continue;
            }
            if (b == '<') {
                return "HTML";
            }
            if (b == '{' || b == '[') {
                return "JSON";
            }
            return null;
        }
        return null;
    }

    /** The header timestamp, or null when absent or zero. */
    public static Instant headerTime(FeedMessage message) {
        if (!message.getHeader().hasTimestamp() || message.getHeader().getTimestamp() <= 0) {
            return null;
        }
        return Instant.ofEpochSecond(message.getHeader().getTimestamp());
    }

    /** True unless the header says DIFFERENTIAL (FULL_DATASET is the proto default). */
    public static boolean fullDataset(FeedMessage message) {
        return message.getHeader().getIncrementality()
                != com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality.DIFFERENTIAL;
    }

    /** {@code value} trimmed, or null when blank; {@link #OVERSIZE} when longer than {@link #MAX_STRING_CHARS}. */
    static String bounded(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > MAX_STRING_CHARS ? OVERSIZE : trimmed;
    }

    /** The marker {@link #bounded} returns for an over-long string; compare by identity. */
    static final String OVERSIZE = new String("\u0000oversize");

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 200), StandardCharsets.UTF_8);
    }
}

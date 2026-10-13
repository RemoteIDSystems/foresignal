/*
 * The band parameter, the stale rule (the feed's own now older than 10 s) and the receiver signal per
 * aircraft are ported in part from God's Eye View, server/providers/local-receivers.js (readFeed,
 * LOCAL_RECEIVER_STALE_MS, LOCAL_RECEIVER_TIMEOUT_MS) and src/sources/adsbRecords.js
 * (normalizeDump1090Aircraft: rssiDbfs, band), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; records come from the shared readsb mapper (observedAt = now − seen, payload in meters);
 * the signal is an Observer's rssi; band is in the payload only for 978 MHz; a stale feed degrades the
 * source's health instead of labeling the feed.
 */
package ai.foresignal.source.adapters.local;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.MapperOptions;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.formats.readsb.ReadsbDocument;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * One {@code local-adsb-{n}} slot (prompt 069): the operator's readsb, dump1090 or skyaware978
 * {@code aircraft.json}, polled over the LAN.
 *
 * <ul>
 *   <li><strong>Params:</strong> {@code url} ({@code LAN_URL}, http or https, path ending in
 *       {@code aircraft.json}, no credentials, query or fragment) and {@code band} ({@code 1090|978}, default
 *       1090).
 *   <li><strong>Schedule:</strong> 2 s (minimum 1 s; more than 5 s is not useful for a local receiver), budget
 *       60/min and 50,000/day, timeout 2 s, at most 4 MB.
 *   <li><strong>Records:</strong> the shared {@link ReadsbAircraftMapper} with {@link
 *       MapperOptions#localReceiver}: {@code observedAt = now − seen} from the document's {@code now}; every
 *       readsb v2 {@code type} mapped to {@code posSource}; {@code nac_p} and {@code nic} in the payload,
 *       {@code nic_baro, sil, gva, sda} (and {@code uat_version} at 978 MHz) in
 *       {@code quality.extra["adsb.*"]}; one {@code Observer(receiverId = sourceId, rssi)} per record;
 *       {@code band: "978"} for a UAT receiver.
 *   <li><strong>Policy (Core-Data-Model §5.3):</strong> TRACK, TTL 5 min, SAMPLED {@code {10s, 120s, 300 m,
 *       thresholds, edgeMin 5s}} with {@code alt_baro} 150 m and {@code track} 10°.
 *   <li><strong>Coverage:</strong> {@code Coverage(receivers = 1, actual = aircraft with a position)}, so the
 *       coverage rule notices a dead antenna.
 *   <li><strong>Staleness:</strong> a document whose {@code now} is more than {@link #STALE_AFTER} older than
 *       its receipt degrades the source's health ("receiver feed stale"); a fresh one clears it.
 * </ul>
 */
public class LocalAdsbAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "local-adsb-";

    /** The URL parameter. */
    public static final String URL_PARAM = "url";

    /** The band parameter. */
    public static final String BAND_PARAM = "band";

    /** A document older than this against its receipt is stale (GEV {@code LOCAL_RECEIVER_STALE_MS}). */
    public static final Duration STALE_AFTER = Duration.ofSeconds(10);

    /** The degraded reason of a stale feed; also the prefix the adapter recognizes when clearing it. */
    public static final String STALE_REASON = "receiver feed stale";

    /** The request timeout (GEV {@code LOCAL_RECEIVER_TIMEOUT_MS}). */
    static final Duration TIMEOUT = Duration.ofSeconds(2);

    /** The response cap. */
    static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    private final SourceDescriptor descriptor;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     */
    public LocalAdsbAdapter(int slot) {
        this.descriptor = buildDescriptor(slot);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The URL must name {@code aircraft.json} and carry no credentials, query or fragment. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        return LocalReceivers.checkJsonUrl(params, URL_PARAM, LocalReceivers.AIRCRAFT_JSON, "aircraft.json");
    }

    /**
     * Polls {@code aircraft.json} once.
     *
     * @throws AdapterException when the receiver cannot be reached or answers something other than readsb JSON
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        URI url = params.lanUrl(URL_PARAM);
        UpstreamResponse response = ctx.http().get(url);
        if (response.truncated()) {
            throw new UpstreamFormatException("aircraft.json is larger than " + MAX_RESPONSE_BYTES + " bytes", "");
        }
        ReadsbDocument document = ReadsbDocument.parse(response.body());
        Instant receivedAt = ctx.now();
        markStaleness(ctx.state(), document.now(), receivedAt);
        String band = params.optionalString(BAND_PARAM).orElse("1090");
        ReadsbAircraftMapper.MapResult mapped =
                ReadsbAircraftMapper.mapAll(document, MapperOptions.localReceiver(ctx.sourceId(), band));
        return FetchResult.complete(mapped.records()).withCoverage(coverage(mapped.records()));
    }

    /** {@code Coverage(receivers = 1, actual = records with a position)}. */
    static Coverage coverage(List<NormalizedRecord> records) {
        int positioned = (int) records.stream().filter(r -> r.geometry() != null).count();
        return new Coverage(1, null, positioned, null);
    }

    /**
     * Degrades the source when the document's {@code now} is more than {@link #STALE_AFTER} before
     * {@code receivedAt}, and clears its own reason otherwise. A receiver clock ahead of ours reads as fresh.
     */
    static void markStaleness(AdapterStateStore state, Instant documentNow, Instant receivedAt) {
        Duration age = Duration.between(documentNow, receivedAt);
        if (age.compareTo(STALE_AFTER) > 0) {
            state.degrade(STALE_REASON + ": aircraft.json is " + age.toSeconds() + " s old");
            return;
        }
        String current = state.get(AdapterStateStore.DEGRADED_REASON_KEY).orElse(null);
        if (current != null && current.startsWith(STALE_REASON)) {
            state.clearDegraded();
        }
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local ADS-B receiver " + slot)
                .description("Aircraft from the operator's own readsb, dump1090 or skyaware978 receiver: polls its"
                        + " aircraft.json (readsb http://host/tar1090/data/aircraft.json, skyaware978"
                        + " http://host:8978/data/aircraft.json). band 978 marks a UAT receiver. Every 2 s by default"
                        + " (1 s minimum; up to 5 s is useful). TRACK, TTL 5 min, observedAt = now - seen. MLAT"
                        + " positions are never redistributable." + LocalReceivers.LOCAL_NOTE)
                .mode(Mode.POLLING)
                .interval(Duration.ofSeconds(1), Duration.ofSeconds(2))
                .defaultBudget(60, 50_000)
                .fetchTimeout(TIMEOUT)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .followRedirects(false)
                .zeroResultsNormal(true)
                .param(ParamSpec.lanUrl(URL_PARAM, "The receiver's aircraft.json on the LAN, for example"
                        + " http://192.168.1.50/tar1090/data/aircraft.json; http or https, no credentials, query or"
                        + " fragment", true, "http", "https"))
                .param(ParamSpec.enumerated(BAND_PARAM, "1090 for Mode S/ADS-B, 978 for UAT (skyaware978)", "1090",
                        List.of("1090", "978")));
        LocalReceivers.local(builder);
        AircraftDescriptors.local(builder, ObservedAtMode.UPSTREAM);
        builder.kindDisplay(AircraftFields.KIND, new KindDisplay("{flight}", "plane", "#0B7285", null,
                AircraftFields.TRACK, null, null));
        return builder.build();
    }
}

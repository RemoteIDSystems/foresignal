package ai.foresignal.chronicle.network;

import ai.foresignal.chronicle.ChronicleCaptureService;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.collab.ShareService;
import ai.foresignal.report.ReportItSupport;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared fixtures of the prompt 134 integration tests: active chronicles over the Ask quakes source, a "war" of quakes
 * at distinct H3 cells each linked to a 116 canonical event with an upstream id (inserted directly, as the evidence
 * resolver would), sketching and discovery driven directly (the schedule is off in tests), and helpers for
 * discoverability, sharing and federated discovery.
 */
public abstract class NetworkItSupport extends ReportItSupport {

    /** A full-capture layer over the Ask quakes source. */
    protected static final String LAYER = "{'name':'Quakes','selector':{'type':'records','sources':['"
            + AskTestSources.QUAKES + "']},'capture':{'mode':'full'}}";

    @Autowired protected ChronicleService chronicles;
    @Autowired protected ChronicleRepository chronicleRows;
    @Autowired protected ChronicleCaptureService capture;
    @Autowired protected ChronicleSketcher sketcher;
    @Autowired protected ChronicleDiscoveryJob discovery;
    @Autowired protected ChronicleNetworkService network;
    @Autowired protected ChronicleAccessService accessRequests;
    @Autowired protected ChronicleLinkRepository linkRows;
    @Autowired protected ChronicleClusterRepository clusterRows;
    @Autowired protected ChronicleAccessRequestRepository requestRows;
    @Autowired protected ChronicleRemoteSketchRepository remoteRows;
    @Autowired protected ChronicleKeyStore keyStore;
    @Autowired protected NetworkSalts salts;
    @Autowired protected FederatedSketches federated;
    @Autowired protected ShareService shares;

    /** An active chronicle of {@code owner} with the quakes layer. */
    protected ChronicleEntity active(Person owner, String name) {
        ChronicleEntity c = chronicles.create(node("{'name':'" + name + "','layers':[" + LAYER + "]}"),
                owner.caller(), ChronicleVocabulary.ORIGIN_OPERATOR, null);
        return chronicles.activate(c.getId(), null, owner.caller(), false);
    }

    /**
     * Ingests {@code n} quakes ({@code prefix0}…), 0.6° of longitude apart (distinct H3 resolution-4 cells), captures
     * them into every active chronicle, and links each to a canonical event of family {@code seismic} with the
     * upstream id {@code {prefix}-U{i}} from originator {@code usgs}.
     */
    protected void war(String prefix, int n, double lon0) {
        for (int i = 0; i < n; i++) {
            quake(prefix + i, "Front " + i, 4.0, lon0 + i * 0.6, 15.0);
        }
        capture.catchUp();
        for (int i = 0; i < n; i++) {
            evidence(AskTestSources.QUAKES, prefix + i, "seismic", "usgs", prefix + "-U" + i, lon0 + i * 0.6, 15.0);
        }
    }

    /** Links a record to a new canonical event, as the 116 resolver would. */
    protected String evidence(String sourceId, String externalId, String family, String originator, String upstreamId,
            double lon, double lat) {
        Instant now = clock.instant();
        String eventId = Ulids.next(now);
        jdbc.update("INSERT INTO canonical_event (id, family, event_time, geom, region_codes, first_observed_at,"
                + " first_observation_ref, observation_count, evidence_group_count, quality_flags, source_ids, status,"
                + " resolver_version, created_at, updated_at) VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326),"
                + " '{}', ?, ?, 1, 1, '{}', '{}', 'open', 'test', ?, ?)", eventId, family, Timestamp.from(now), lon,
                lat, Timestamp.from(now), "record:" + sourceId + "/" + externalId, Timestamp.from(now),
                Timestamp.from(now));
        jdbc.update("INSERT INTO observation_link (id, canonical_event_id, source_id, external_id, seq, version,"
                + " originator_id, independence_root, upstream_event_id, link_confidence, link_reason, event_time,"
                + " observed_at, lon, lat, linked_at) VALUES (?, ?, ?, ?, ?, 1, ?, ?, ?, 1.0, '{}'::jsonb, ?, ?, ?,"
                + " ?, ?)", Ulids.next(now), eventId, sourceId, externalId, headSeq(sourceId, externalId), originator,
                originator, upstreamId, Timestamp.from(now), Timestamp.from(now), lon, lat, Timestamp.from(now));
        return eventId;
    }

    /** Sets a chronicle's discoverability as its owner. */
    protected void discoverable(Person owner, ChronicleEntity c, String level) {
        network.setDiscoverability(c.getId(), node("{'discoverability':'" + level + "'}"), owner.caller());
    }

    /** Sets a chronicle's discoverability and reveal flags as its owner. */
    protected void discoverable(Person owner, ChronicleEntity c, String level, boolean revealTitle,
            boolean revealOwner) {
        network.setDiscoverability(c.getId(), node("{'discoverability':'" + level + "','revealTitle':" + revealTitle
                + ",'revealOwner':" + revealOwner + "}"), owner.caller());
    }

    /** Shares a chronicle with a person as viewer. */
    protected void shareWith(Person owner, ChronicleEntity c, Person viewer) {
        shares.share("chronicle:" + c.getId(), node("{'grantees':['principal:" + viewer.id() + "'],'role':'viewer'}"),
                owner.caller(), "test");
    }

    /** Refreshes every sketch and runs discovery. */
    protected ChronicleDiscoveryJob.Result sketchAndDiscover() {
        sketcher.refreshAll();
        return discovery.run();
    }

    /** Turns federated discovery on or off. */
    protected void federatedDiscovery(boolean on) {
        settings.put(SettingKeys.NETWORK_FEDERATED, on);
    }
}

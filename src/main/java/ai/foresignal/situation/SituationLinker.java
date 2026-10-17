package ai.foresignal.situation;

import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.TriggerEngine;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The deterministic situation linker {@code situation-linker 1.0.0} (prompt 118 §2): a {@link ChangeBus}
 * subscriber with the durable cursor {@code situation.cursor} (a retention consumer through
 * {@link SituationCursorConsumer}) that catches up in {@value #CHUNK}-seq chunks on its own virtual thread and
 * never blocks ingest.
 *
 * <p>For each input ({@link LinkerInputReader}) in seq order:
 *
 * <ol>
 *   <li>an input that is already a member of a situation, directly or through another member (an intelligence
 *       event pinning its record, or the record an alert fired on), is not linked again (exactly-once across
 *       restarts and rewinds; an operator moves members explicitly);
 *   <li>the candidates are the open situations active within {@code dormancyWindow} (dormant ones within
 *       {@code reopenWindow}) of the input's observation time that pass the cheap prefilter;
 *   <li>each is scored ({@link SituationScorer}); the best joins when its score ≥ {@code linkThreshold} (ties by
 *       situation id);
 *   <li>otherwise an active intelligence event of severity ≥ warning, a significant finding or a critical alert
 *       seeds a new {@code emerging} situation titled "{type title} — {region name}"; anything else is skipped.
 * </ol>
 *
 * <p>Every decision is reproducible from the input, the candidate situations at that seq and the linker version;
 * {@link #dryRun} runs the same procedure into memory over a seq range ({@code POST /v1/situations/linker/test}).
 * The chunk end never passes the trigger engine's cursor, so the alerts of a seq are written before the linker
 * reads them. A failing input is logged and skipped; the cursor advances.
 */
@Component
public class SituationLinker implements ChangeListener, DisposableBean, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(SituationLinker.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "situation-linker";

    /** The seq chunk processed per pass. */
    public static final long CHUNK = 10_000;

    /** The widest seq range a dry run covers. */
    public static final long MAX_TEST_RANGE = 100_000;

    /** The most decisions a dry run returns. */
    public static final int MAX_TEST_DECISIONS = 1_000;

    /** The candidates listed per decision. */
    static final int LISTED_CANDIDATES = 5;

    private final ChangeBus changeBus;
    private final AppSettingService settings;
    private final SituationSettings situationSettings;
    private final LinkerInputReader reader;
    private final SituationService service;
    private final SituationSimilarity similarity;
    private final AttentionTopicRepository topics;
    private final SituationMemberRepository members;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<TriggerEngine> triggers;
    private final ObjectProvider<ManifestService> manifests;
    private final boolean autoStart;

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;

    /** Creates the linker. */
    public SituationLinker(ChangeBus changeBus, AppSettingService settings, SituationSettings situationSettings,
            LinkerInputReader reader, SituationService service, SituationSimilarity similarity,
            AttentionTopicRepository topics, SituationMemberRepository members, JdbcTemplate jdbc,
            ObjectProvider<TriggerEngine> triggers, ObjectProvider<ManifestService> manifests,
            @Value("${foresignal.situations.auto-start:true}") boolean autoStart) {
        this.changeBus = changeBus;
        this.settings = settings;
        this.situationSettings = situationSettings;
        this.reader = reader;
        this.service = service;
        this.similarity = similarity;
        this.topics = topics;
        this.members = members;
        this.jdbc = jdbc;
        this.triggers = triggers;
        this.manifests = manifests;
        this.autoStart = autoStart;
    }

    /** Starts the subscriber and the catch-up thread once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.SITUATIONS)
    public synchronized void start() {
        if (running || !autoStart) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 1024);
        thread = Thread.ofVirtual().name("situation-linker").start(this::loop);
    }

    /** Stops the linker. */
    @Override
    public synchronized void destroy() {
        running = false;
        if (subscription != null) {
            subscription.close();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        // The catch-up loop advances the cursor.
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // The loop catches up from the durable cursor.
    }

    /** True while the background loop runs. */
    public boolean running() {
        return running;
    }

    /** The durable cursor. */
    public long cursor() {
        if (cursor < 0) {
            cursor = settings.get(SettingKeys.SITUATION_CURSOR, Long.class).orElse(0L);
        }
        return cursor;
    }

    /** Drops the cached cursor so it is read again from {@code app_setting}. */
    @Override
    public void resetState() {
        cursor = -1;
    }

    /** Moves the durable cursor back to {@code seq} (tests, and a restart simulated in place). */
    public synchronized void rewind(long seq) {
        storeCursor(seq);
    }

    private void loop() {
        while (running) {
            try {
                catchUp();
            } catch (RuntimeException e) {
                log.warn("Situation linker catch-up failed: {}", e.toString());
            }
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    /** Links every input between the cursor and the watermark (never past the trigger engine's cursor). */
    public synchronized void catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        TriggerEngine engine = triggers.getIfAvailable();
        if (engine != null && engine.running()) {
            watermark = Math.min(watermark, engine.cursor());
        }
        long from = cursor();
        Live state = new Live();
        while (from < watermark) {
            long to = Math.min(from + CHUNK, watermark);
            List<Decision> decisions = new ArrayList<>();
            for (LinkerInput input : reader.read(from, to)) {
                try {
                    decisions.add(decide(input, state));
                } catch (RuntimeException e) {
                    log.warn("Situation linking failed for {} at seq {}: {}", input.objectRef(), input.seq(),
                            e.toString());
                }
            }
            recordManifest(from, to, decisions);
            from = to;
            storeCursor(from);
        }
    }

    // ------------------------------------------------------------------------- the procedure

    /**
     * One linker decision.
     *
     * @param input the input
     * @param action {@code join}, {@code seed}, {@code skip} or {@code already-member}
     * @param situationId the joined, seeded or holding situation, or null
     * @param seedTitle the title of a seeded situation, or null
     * @param score the best score, or null when no candidate passed the prefilter
     * @param joinReason the stored join reason, or null
     * @param candidates the best scored candidates (situation id → score), up to five
     */
    public record Decision(LinkerInput input, String action, String situationId, String seedTitle, Double score,
            Map<String, Object> joinReason, List<Map<String, Object>> candidates) {

        /** The {@code SituationLinkDecision} shape. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("seq", input.seq());
            out.put("inputRef", input.objectRef());
            out.put("memberType", input.memberType());
            out.put("signalFamily", input.signalFamily());
            out.put("action", action);
            out.put("situationId", situationId);
            out.put("seedTitle", seedTitle);
            out.put("score", score);
            out.put("joinReason", joinReason);
            out.put("candidates", candidates);
            return out;
        }
    }

    /** The state the procedure reads and changes: the database, or an in-memory overlay of it. */
    interface State {

        /** The candidate situations at {@code at}. */
        List<SituationScorer.Candidate> candidates(Instant at);

        /** The situations that already hold the input. */
        Set<String> holders(LinkerInput input);

        /** Seeds a situation and returns its id. */
        String seed(LinkerInput input, String title, Map<String, Object> reason);

        /** Joins the input to a situation. */
        void join(String situationId, LinkerInput input, Map<String, Object> reason);

        /** The text-similarity cosine, or null. */
        Double cosine(LinkerInput input, String situationId);
    }

    /** Decides (and, through {@code state}, applies) one input at the configured link threshold. */
    Decision decide(LinkerInput input, State state) {
        return decide(input, state, situationSettings.linkThreshold());
    }

    /** Decides (and, through {@code state}, applies) one input at {@code threshold}. */
    static Decision decide(LinkerInput input, State state, double threshold) {
        Set<String> holders = state.holders(input);
        if (!holders.isEmpty()) {
            return new Decision(input, "already-member", holders.iterator().next(), null, null, null, List.of());
        }
        LinkerWeights weights = LinkerWeights.DEFAULT;
        List<Scored> scored = new ArrayList<>();
        for (SituationScorer.Candidate candidate : state.candidates(input.observedAt())) {
            if (!SituationScorer.prefilter(input, candidate, weights)) {
                continue;
            }
            SituationScorer.Score score = SituationScorer.score(input, candidate, state.cosine(input, candidate.id()),
                    weights);
            scored.add(new Scored(candidate.id(), score));
        }
        scored.sort(Comparator.comparingDouble((Scored s) -> s.score().score()).reversed()
                .thenComparing(Scored::situationId));
        List<Map<String, Object>> listed = new ArrayList<>();
        for (Scored s : scored.subList(0, Math.min(LISTED_CANDIDATES, scored.size()))) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("situationId", s.situationId());
            entry.put("score", s.score().score());
            listed.add(entry);
        }
        Scored best = scored.isEmpty() ? null : scored.get(0);
        if (best != null && best.score().score() >= threshold) {
            Map<String, Object> reason = reason("linker", best.score());
            state.join(best.situationId(), input, reason);
            return new Decision(input, "join", best.situationId(), null, best.score().score(), reason, listed);
        }
        if (input.seedEligible()) {
            String title = seedTitle(input);
            Map<String, Object> reason = reason("seed", best == null ? null : best.score());
            String id = state.seed(input, title, reason);
            return new Decision(input, "seed", id, title, best == null ? null : best.score().score(), reason, listed);
        }
        return new Decision(input, "skip", null, null, best == null ? null : best.score().score(), null, listed);
    }

    private record Scored(String situationId, SituationScorer.Score score) {}

    /** The join reason of a linker decision. */
    static Map<String, Object> reason(String method, SituationScorer.Score score) {
        Map<String, Object> reason = new LinkedHashMap<>();
        reason.put("method", method);
        reason.put("score", score == null ? null : score.score());
        reason.put("components", score == null ? null : score.components());
        reason.put("linkerVersion", SituationScorer.VERSION);
        reason.put("actor", SituationService.LINKER_ACTOR);
        return reason;
    }

    /** "{type title} — {region name}", with the first region code or "unlocated" when no name is known. */
    static String seedTitle(LinkerInput input) {
        String region = input.regionName();
        if (region == null || region.isBlank()) {
            region = input.regionCodes() == null || input.regionCodes().isEmpty() ? "unlocated"
                    : input.regionCodes().iterator().next();
        }
        String type = input.typeTitle() == null || input.typeTitle().isBlank() ? "Situation" : input.typeTitle();
        String title = type + " — " + region;
        return title.length() > SituationService.MAX_TITLE ? title.substring(0, SituationService.MAX_TITLE) : title;
    }

    // ------------------------------------------------------------------------- dry run

    /**
     * Runs the linker over {@code (fromSeq, toSeq]} into memory and returns the would-be decisions; nothing is
     * stored. Starting from the situations stored now, the result equals what the live linker does over the same
     * range (seeded situations get would-be ids).
     *
     * @param privateAccess whether decisions about private inputs may be returned
     * @return the decisions, and the number of inputs considered
     */
    public DryRun dryRun(long fromSeq, long toSeq, boolean privateAccess) {
        Overlay state = new Overlay();
        List<Decision> decisions = new ArrayList<>();
        List<LinkerInput> inputs = reader.read(fromSeq, toSeq);
        for (LinkerInput input : inputs) {
            Decision decision = decide(input, state);
            if (!privateAccess && reader.isPrivate(input)) {
                continue;
            }
            decisions.add(decision);
        }
        return new DryRun(inputs.size(), decisions);
    }

    /**
     * A dry run's result.
     *
     * @param inputsConsidered the inputs read
     * @param decisions the decisions the caller may see
     */
    public record DryRun(int inputsConsidered, List<Decision> decisions) {}

    /**
     * Runs the linker procedure over {@code inputs} from an empty state, entirely in memory: no stored situation is
     * read and nothing is written (prompt 123, the benchmark harness's isolated mode of the dry run). Text similarity
     * is off (no embeddings exist for synthetic inputs); seeded situations get would-be ids from the inputs' times.
     *
     * @param inputs the inputs, in seq order
     * @param threshold the link threshold
     * @param dormancy the dormancy window
     * @param reopen the reopen window
     * @return one decision per input, in input order
     */
    public static List<Decision> simulate(List<LinkerInput> inputs, double threshold, Duration dormancy,
            Duration reopen) {
        Isolated state = new Isolated(dormancy, reopen);
        List<Decision> decisions = new ArrayList<>();
        for (LinkerInput input : inputs) {
            decisions.add(decide(input, state, threshold));
        }
        return decisions;
    }

    // ------------------------------------------------------------------------- states

    /** The keywords of the given topics. */
    private List<String> keywords(List<String> topicIds) {
        List<String> out = new ArrayList<>();
        if (topicIds == null || topicIds.isEmpty()) {
            return out;
        }
        try {
            for (AttentionTopicEntity topic : topics.findAllById(topicIds)) {
                if (topic.getKeywords() != null) {
                    out.addAll(topic.getKeywords());
                }
            }
        } catch (RuntimeException e) {
            return out;
        }
        return out;
    }

    private SituationScorer.Candidate candidate(SituationEntity situation) {
        return new SituationScorer.Candidate(situation.getId(), situation.getTheme(), situation.getStatus(),
                new TreeSet<>(situation.getRegionCodes()), situation.getGeom(), new TreeSet<>(situation.getEntityIds()),
                keywords(situation.getTopicIds()), situation.getLastActivityAt());
    }

    /** The database: candidates and holders are read, seeds and joins written through {@link SituationService}. */
    final class Live implements State {

        @Override
        public List<SituationScorer.Candidate> candidates(Instant at) {
            List<SituationScorer.Candidate> out = new ArrayList<>();
            for (SituationEntity situation : service.candidates(at, situationSettings.dormancyWindow(),
                    situationSettings.reopenWindow())) {
                out.add(candidate(situation));
            }
            return out;
        }

        @Override
        public Set<String> holders(LinkerInput input) {
            return service.holders(input);
        }

        @Override
        public String seed(LinkerInput input, String title, Map<String, Object> reason) {
            return service.seed(input, title, reason).getId();
        }

        @Override
        public void join(String situationId, LinkerInput input, Map<String, Object> reason) {
            service.join(situationId, input, reason);
        }

        @Override
        public Double cosine(LinkerInput input, String situationId) {
            return similarity.cosine(input.embeddingDocId(), situationId);
        }
    }

    /** A simulated situation of the dry run. */
    private static final class Sim {
        String id;
        String theme;
        String status;
        Set<String> regions = new TreeSet<>();
        Set<String> entities = new TreeSet<>();
        List<Geometry> geometries = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        Instant lastActivity;
        boolean seeded;

        SituationScorer.Candidate candidate() {
            return new SituationScorer.Candidate(id, theme, status, new TreeSet<>(regions),
                    SituationGeometry.of(geometries), new TreeSet<>(entities), keywords, lastActivity);
        }
    }

    /**
     * The dry run's state: the stored situations, overlaid with the joins and seeds made so far in memory, applying
     * the same derivations ({@link SituationGeometry}, regions, entities, last activity, dormant → active) the live
     * path stores.
     */
    final class Overlay implements State {

        private final Map<String, Sim> sims = new HashMap<>();
        private final Map<String, Set<String>> heldBy = new HashMap<>();

        @Override
        public List<SituationScorer.Candidate> candidates(Instant at) {
            Map<String, SituationScorer.Candidate> out = new LinkedHashMap<>();
            for (SituationEntity situation : service.candidates(at, situationSettings.dormancyWindow(),
                    situationSettings.reopenWindow())) {
                out.put(situation.getId(), sims.containsKey(situation.getId())
                        ? sims.get(situation.getId()).candidate() : candidate(situation));
            }
            Duration dormancy = situationSettings.dormancyWindow();
            Duration reopen = situationSettings.reopenWindow();
            for (Sim sim : sims.values()) {
                if (out.containsKey(sim.id) || !SituationLifecycle.OPEN.contains(sim.status)) {
                    continue;
                }
                Duration window = SituationLifecycle.DORMANT.equals(sim.status) ? reopen : dormancy;
                if (sim.lastActivity != null && !sim.lastActivity.isBefore(at.minus(window))) {
                    out.put(sim.id, sim.candidate());
                }
            }
            List<SituationScorer.Candidate> list = new ArrayList<>(out.values());
            list.sort(Comparator.comparing(SituationScorer.Candidate::id));
            return list;
        }

        @Override
        public Set<String> holders(LinkerInput input) {
            Set<String> out = new LinkedHashSet<>(service.holders(input));
            out.addAll(heldBy.getOrDefault(input.linkKey(), Set.of()));
            for (String key : service.indirectKeys(input)) {
                out.addAll(heldBy.getOrDefault(key, Set.of()));
            }
            return out;
        }

        @Override
        public String seed(LinkerInput input, String title, Map<String, Object> reason) {
            Sim sim = new Sim();
            sim.id = Ulids.next(input.observedAt() == null ? Instant.now() : input.observedAt());
            sim.theme = input.theme() == null ? SituationThemes.MIXED : input.theme();
            sim.status = SituationLifecycle.EMERGING;
            sim.lastActivity = input.observedAt();
            sim.seeded = true;
            sims.put(sim.id, sim);
            apply(sim, input);
            return sim.id;
        }

        @Override
        public void join(String situationId, LinkerInput input, Map<String, Object> reason) {
            Sim sim = sims.computeIfAbsent(situationId, this::load);
            if ("core".equals(input.role()) && SituationLifecycle.DORMANT.equals(sim.status)) {
                sim.status = SituationLifecycle.ACTIVE;
            }
            apply(sim, input);
        }

        @Override
        public Double cosine(LinkerInput input, String situationId) {
            Sim sim = sims.get(situationId);
            return sim != null && sim.seeded ? null : similarity.cosine(input.embeddingDocId(), situationId);
        }

        private void apply(Sim sim, LinkerInput input) {
            sim.regions.addAll(input.regionCodes());
            sim.entities.addAll(input.entityIds());
            if (input.geom() != null) {
                sim.geometries.add(input.geom());
            }
            if ("core".equals(input.role()) && input.observedAt() != null
                    && (sim.lastActivity == null || input.observedAt().isAfter(sim.lastActivity))) {
                sim.lastActivity = input.observedAt();
            }
            heldBy.computeIfAbsent(input.linkKey(), k -> new LinkedHashSet<>()).add(sim.id);
        }

        private Sim load(String situationId) {
            SituationEntity situation = service.get(situationId);
            Sim sim = new Sim();
            sim.id = situation.getId();
            sim.theme = situation.getTheme();
            sim.status = situation.getStatus();
            sim.lastActivity = situation.getLastActivityAt();
            sim.entities.addAll(situation.getEntityIds());
            sim.keywords.addAll(keywords(situation.getTopicIds()));
            List<SituationMemberEntity> current = members.findBySituationIdOrderByAddedAtAscIdAsc(situationId);
            boolean located = false;
            for (SituationMemberEntity member : current) {
                if (member.getGeom() != null) {
                    sim.geometries.add(member.getGeom());
                }
                if (!member.getRegionCodes().isEmpty()) {
                    located = true;
                    sim.regions.addAll(member.getRegionCodes());
                }
            }
            if (!located) {
                sim.regions.addAll(situation.getRegionCodes());
            }
            return sim;
        }
    }

    /**
     * The isolated state of {@link #simulate}: simulated situations only, with the same derivations as the
     * {@link Overlay} (geometry, regions, entities, last activity, dormant to active), and no database.
     */
    static final class Isolated implements State {

        private final Map<String, Sim> sims = new LinkedHashMap<>();
        private final Map<String, Set<String>> heldBy = new HashMap<>();
        private final Duration dormancy;
        private final Duration reopen;
        private int counter;

        Isolated(Duration dormancy, Duration reopen) {
            this.dormancy = dormancy;
            this.reopen = reopen;
        }

        @Override
        public List<SituationScorer.Candidate> candidates(Instant at) {
            List<SituationScorer.Candidate> out = new ArrayList<>();
            for (Sim sim : sims.values()) {
                if (!SituationLifecycle.OPEN.contains(sim.status)) {
                    continue;
                }
                Duration window = SituationLifecycle.DORMANT.equals(sim.status) ? reopen : dormancy;
                if (at == null || sim.lastActivity == null || !sim.lastActivity.isBefore(at.minus(window))) {
                    out.add(sim.candidate());
                }
            }
            out.sort(Comparator.comparing(SituationScorer.Candidate::id));
            return out;
        }

        @Override
        public Set<String> holders(LinkerInput input) {
            return new LinkedHashSet<>(heldBy.getOrDefault(input.linkKey(), Set.of()));
        }

        @Override
        public String seed(LinkerInput input, String title, Map<String, Object> reason) {
            Sim sim = new Sim();
            counter++;
            sim.id = String.format("sim-%06d", counter);
            sim.theme = input.theme() == null ? SituationThemes.MIXED : input.theme();
            sim.status = SituationLifecycle.EMERGING;
            sim.lastActivity = input.observedAt();
            sim.seeded = true;
            sims.put(sim.id, sim);
            apply(sim, input);
            return sim.id;
        }

        @Override
        public void join(String situationId, LinkerInput input, Map<String, Object> reason) {
            Sim sim = sims.get(situationId);
            if (sim == null) {
                return;
            }
            if ("core".equals(input.role()) && SituationLifecycle.DORMANT.equals(sim.status)) {
                sim.status = SituationLifecycle.ACTIVE;
            }
            apply(sim, input);
        }

        @Override
        public Double cosine(LinkerInput input, String situationId) {
            return null;
        }

        private void apply(Sim sim, LinkerInput input) {
            sim.regions.addAll(input.regionCodes());
            sim.entities.addAll(input.entityIds());
            if (input.geom() != null) {
                sim.geometries.add(input.geom());
            }
            if ("core".equals(input.role()) && input.observedAt() != null
                    && (sim.lastActivity == null || input.observedAt().isAfter(sim.lastActivity))) {
                sim.lastActivity = input.observedAt();
            }
            heldBy.computeIfAbsent(input.linkKey(), k -> new LinkedHashSet<>()).add(sim.id);
        }
    }

    // ------------------------------------------------------------------------- manifests and cursor

    /** Records the manifest of a chunk that made joins or seeds, and stores it on the situations touched. */
    private void recordManifest(long from, long to, List<Decision> decisions) {
        ManifestService service = manifests.getIfAvailable();
        Set<String> touched = new TreeSet<>();
        for (Decision decision : decisions) {
            if (("join".equals(decision.action()) || "seed".equals(decision.action())) && decision.situationId() != null) {
                touched.add(decision.situationId());
            }
        }
        if (service == null || touched.isEmpty()) {
            return;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("weights", LinkerWeights.DEFAULT.toMap());
            config.put("linkThreshold", situationSettings.linkThreshold());
            config.put("dormancyWindow", situationSettings.dormancyWindow().toString());
            config.put("reopenWindow", situationSettings.reopenWindow().toString());
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("fromSeq", from);
            query.put("toSeq", to);
            query.put("decisions", decisions.size());
            String id = service.record(ManifestRequest.of(SituationScorer.NAME, SituationScorer.VERSION).config(config)
                    .registries("themes.json", "originators.json", "canonical-rules.json").asOf(to).query(query));
            jdbc.update("UPDATE situation SET manifest_id = ? WHERE id = ANY(?::text[])", id,
                    touched.toArray(String[]::new));
        } catch (RuntimeException e) {
            log.warn("Could not record the situation linker manifest of seq {}..{}: {}", from, to, e.toString());
        }
    }

    private void storeCursor(long seq) {
        cursor = seq;
        settings.put(SettingKeys.SITUATION_CURSOR, seq);
    }
}

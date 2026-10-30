package ai.foresignal.briefing;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.Scope;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Briefing management and on-demand runs (prompt 029): the service behind the {@code Briefings}
 * operations, the briefing MCP tools, Ask's {@code create_briefing} action and settings import.
 *
 * <ul>
 *   <li><b>Writes</b> validate with {@link BriefingValidator} and persist {@code next_run_at} (null
 *       while disabled). The creating or replacing token's scope becomes the owner scope.
 *   <li><b>One run at a time:</b> a run claims {@code running_since} with a conditional update, so a
 *       second run of the same briefing (scheduler or {@code runBriefing}, on any instance) is refused
 *       with {@code 409 BRIEFING_RUNNING} until the first finishes or its mark goes stale.
 *   <li><b>{@code runBriefing}</b> waits up to {@code foresignal.briefing.run-timeout} (120 s) and
 *       returns the run; a run still going then is returned {@code running} and completes in the
 *       background.
 *   <li><b>Previews</b> render without storing, delivering or touching the schedule.
 *   <li><b>Editions</b> (prompt NP-003): a briefing created by a signed-in user, or a user's own API token, is that
 *       user's edition ({@code owner_user_id}). Only its owner sees, runs, changes or deletes it, and an instance
 *       (non-user) token as the operator's credential; any other user and an anonymous caller get {@code 404},
 *       as for a briefing that does not exist ({@link #visibleTo(BriefingEntity, EditionViewer)}).
 * </ul>
 */
@Service
public class BriefingService {

    /** The shared private-source visibility (prompt 039); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.security.visibility.SourceVisibility visibility;


    private static final Logger log = LoggerFactory.getLogger(BriefingService.class);

    /** The default page size of {@code listBriefingRuns}. */
    public static final int DEFAULT_RUN_PAGE = 500;

    /** The largest page size of {@code listBriefingRuns}. */
    public static final int MAX_RUN_PAGE = 5000;

    private final BriefingRepository briefings;
    private final BriefingRunRepository runs;
    private final BriefingValidator validator;
    private final BriefingRunner runner;
    private final BriefingProperties properties;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the service. */
    public BriefingService(BriefingRepository briefings, BriefingRunRepository runs, BriefingValidator validator,
            BriefingRunner runner, BriefingProperties properties, JdbcTemplate jdbc, Clock clock) {
        this.briefings = briefings;
        this.runs = runs;
        this.validator = validator;
        this.runner = runner;
        this.properties = properties;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Stops accepting runs. */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    /** Keeps edition sites' keys (prompt NP-005); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.beans.factory.ObjectProvider<EditionSiteService> editionSites;

    /** Builds site URLs (prompt NP-005); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private BriefingLinks links;

    /** Mints or revokes an edition's site keys after a save (NP-005). */
    void syncSite(BriefingEntity saved) {
        EditionSiteService sites = editionSites == null ? null : editionSites.getIfAvailable();
        if (sites != null) {
            sites.sync(saved);
        }
    }

    /** Maps a caller's principal to its user (prompt NP-003); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.security.user.UserRepository users;

    /**
     * Who asks about an edition (prompt NP-003).
     *
     * @param userId the calling user (a session or the user's own API token), or null
     * @param anonymous true for a caller without credentials (open reads)
     */
    public record EditionViewer(UUID userId, boolean anonymous) {

        /** An instance (non-user) credential: the operator, which sees every briefing. */
        public static final EditionViewer INSTANCE = new EditionViewer(null, false);

        /** A caller without credentials. */
        public static final EditionViewer ANONYMOUS = new EditionViewer(null, true);

        /**
         * The viewer of an HTTP request: its session's user, else its token's owner, else an instance credential or
         * an anonymous caller.
         *
         * @param request the request
         * @param anonymous true when the request carries no credentials
         */
        public static EditionViewer of(jakarta.servlet.http.HttpServletRequest request, boolean anonymous) {
            UUID user = ai.foresignal.security.user.AuthenticatedSession.of(request)
                    .map(ai.foresignal.security.user.AuthenticatedSession::userId)
                    .or(() -> ai.foresignal.security.user.TokenOwner.of(request)
                            .map(ai.foresignal.security.user.TokenOwner::userId))
                    .orElse(null);
            return user != null ? new EditionViewer(user, false) : anonymous ? ANONYMOUS : INSTANCE;
        }
    }

    /**
     * The viewer of an MCP or REST caller: the user whose principal the caller's token (or session) carries, else an
     * instance credential, or anonymous without a token.
     */
    public EditionViewer viewer(McpCaller caller) {
        if (caller == null || caller.anonymous()) {
            return EditionViewer.ANONYMOUS;
        }
        String principal = caller.token().principalId();
        if (users == null || principal == null || caller.token().bootstrap()) {
            return EditionViewer.INSTANCE;
        }
        return users.findByPrincipalId(principal).map(u -> new EditionViewer(u.getId(), false))
                .orElse(EditionViewer.INSTANCE);
    }

    /**
     * True when {@code viewer} may see and act on {@code entity} (prompt NP-003): any viewer for a briefing no user
     * owns; for an edition, its owner or an instance credential.
     */
    public static boolean visibleTo(BriefingEntity entity, EditionViewer viewer) {
        UUID owner = entity.getOwnerUserId();
        if (owner == null || viewer == null) {
            return owner == null || viewer == null;
        }
        if (viewer.anonymous()) {
            return false;
        }
        return viewer.userId() == null || viewer.userId().equals(owner);
    }

    /**
     * Throws {@code 404} unless briefing {@code id} is visible under {@code restriction} (prompt 035) and to
     * {@code viewer} (prompt NP-003).
     */
    public void requireVisible(UUID id, ai.foresignal.security.restrict.ReadRestriction restriction,
            EditionViewer viewer) {
        requireVisible(id, restriction);
        Optional<BriefingEntity> entity = briefings.findById(id);
        if (entity.isPresent() && !visibleTo(entity.get(), viewer)) {
            throw NotFoundException.of("Briefing", id);
        }
    }

    /** {@code listBriefings} for {@code viewer} under {@code restriction}: only the briefings visible to both. */
    public List<Map<String, Object>> list(ai.foresignal.security.restrict.ReadRestriction restriction,
            EditionViewer viewer) {
        return briefings.findAllByOrderByCreatedAtAscIdAsc().stream()
                .filter(b -> visibleTo(b, restriction) && visibleTo(b, viewer))
                .map(this::view)
                .toList();
    }

    /**
     * {@code listMyEditions}: the editions {@code userId} owns, newest first.
     *
     * @param userId the calling user
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     * @return {@code {items: EditionSummary[], nextCursor}}
     */
    public Map<String, Object> listMine(UUID userId, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id, created_at FROM briefing WHERE owner_user_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(userId);
        if (cursor != null && !cursor.isBlank()) {
            Position after = decode(cursor);
            sql.append(" AND (created_at < ? OR (created_at = ? AND id < ?))");
            args.add(Timestamp.from(after.startedAt()));
            args.add(Timestamp.from(after.startedAt()));
            args.add(after.id());
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ?");
        args.add(limit + 1);
        List<UUID> ids = jdbc.query(sql.toString(), (rs, i) -> rs.getObject(1, UUID.class), args.toArray());
        boolean more = ids.size() > limit;
        List<UUID> page = more ? ids.subList(0, limit) : ids;
        Map<UUID, BriefingEntity> loaded = new LinkedHashMap<>();
        briefings.findAllById(page).forEach(b -> loaded.put(b.getId(), b));
        List<Map<String, Object>> items = new ArrayList<>();
        BriefingEntity last = null;
        for (UUID id : page) {
            BriefingEntity b = loaded.get(id);
            if (b != null) {
                last = b;
                items.add(BriefingViews.editionSummary(b, runs.findFirstByBriefingIdAndStatusNotOrderByStartedAtDescIdDesc(
                        id, BriefingRunEntity.RUNNING).map(BriefingRunEntity::getId).orElse(null),
                        links == null ? null : links.editionSite(id)));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more && last != null ? encode(last.getCreatedAt(), last.getId()) : null);
        return out;
    }

    // ------------------------------------------------------------------------- definitions

    /** {@code listBriefings}: every briefing, oldest first. */
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (BriefingEntity entity : briefings.findAllByOrderByCreatedAtAscIdAsc()) {
            out.add(view(entity));
        }
        return out;
    }

    /** {@code getBriefing}. */
    public Map<String, Object> get(UUID id) {
        return view(load(id));
    }

    /**
     * True when a caller under {@code restriction} may see {@code entity} and its runs: its
     * creator's restriction lies within the caller's (prompt 035), so no run can show the caller
     * more than its own restriction allows.
     */
    public static boolean visibleTo(BriefingEntity entity, ai.foresignal.security.restrict.ReadRestriction restriction) {
        return restriction == null || !restriction.hasDataRestrictions()
                || ai.foresignal.security.restrict.ReadRestriction.of(entity.getRestrictionSnapshot(), false).within(restriction);
    }

    /**
     * Throws {@code 404} unless briefing {@code id} is visible under {@code restriction} (see
     * {@link #visibleTo}).
     */
    public void requireVisible(UUID id, ai.foresignal.security.restrict.ReadRestriction restriction) {
        if (restriction != null && restriction.hasDataRestrictions() && !visibleTo(load(id), restriction)) {
            throw NotFoundException.of("Briefing", id);
        }
    }

    /** {@code listBriefings} for a caller under {@code restriction}: only visible briefings. */
    public List<Map<String, Object>> list(ai.foresignal.security.restrict.ReadRestriction restriction) {
        if (restriction == null || !restriction.hasDataRestrictions()) {
            return list();
        }
        return briefings.findAllByOrderByCreatedAtAscIdAsc().stream()
                .filter(b -> visibleTo(b, restriction))
                .map(this::view)
                .toList();
    }

    /** The stored briefing, or 404. */
    public BriefingEntity load(UUID id) {
        return briefings.findById(id).orElseThrow(() -> NotFoundException.of("Briefing", id));
    }

    /**
     * {@code createBriefing}.
     *
     * @param body the {@code BriefingInput}
     * @param caller who creates it (becomes the owner)
     * @return the {@code Briefing} with its warnings
     */
    public Map<String, Object> create(JsonNode body, McpCaller caller) {
        BriefingValidator.Validated valid = validator.validate(body, caller.excludedSources());
        BriefingEntity entity = new BriefingEntity(UUID.randomUUID());
        Instant now = clock.instant();
        entity.setCreatedAt(now);
        apply(entity, valid.input(), owner(caller), scope(caller), now);
        entity.setRestrictionSnapshot(caller.restriction().toStored());
        entity.setOwnerUserId(viewer(caller).userId());
        BriefingEntity saved = briefings.saveAndFlush(entity);
        syncSite(saved);
        return BriefingViews.briefing(saved, valid.warnings());
    }

    /**
     * {@code updateBriefing}: replaces the definition whole; past runs are kept.
     *
     * @param id the briefing
     * @param body the {@code BriefingInput}
     * @param caller who replaces it (becomes the owner)
     */
    public Map<String, Object> update(UUID id, JsonNode body, McpCaller caller) {
        BriefingEntity entity = load(id);
        if (!visibleTo(entity, viewer(caller))) {
            throw NotFoundException.of("Briefing", id);
        }
        BriefingValidator.Validated valid = validator.validate(body, caller.excludedSources());
        apply(entity, valid.input(), owner(caller), scope(caller), clock.instant());
        entity.setRestrictionSnapshot(caller.restriction().toStored());
        BriefingEntity saved = briefings.saveAndFlush(entity);
        syncSite(saved);
        return BriefingViews.briefing(saved, valid.warnings());
    }

    /**
     * Creates or replaces the briefing {@code id} with an {@code admin} owner (settings import, 019).
     *
     * @throws ValidationException when the input is not valid
     */
    public Map<String, Object> upsert(UUID id, JsonNode body) {
        BriefingValidator.Validated valid = validator.validate(body, Set.of());
        Instant now = clock.instant();
        BriefingEntity entity = briefings.findById(id).orElseGet(() -> {
            BriefingEntity created = new BriefingEntity(id);
            created.setCreatedAt(now);
            return created;
        });
        apply(entity, valid.input(), null, Scope.ADMIN.wireValue(), now);
        entity.setRestrictionSnapshot(null);
        BriefingEntity saved = briefings.saveAndFlush(entity);
        syncSite(saved);
        return BriefingViews.briefing(saved, valid.warnings());
    }

    /** {@code deleteBriefing}: its runs are kept until retention removes them. */
    public void delete(UUID id) {
        delete(id, EditionViewer.INSTANCE);
    }

    /** {@code deleteBriefing} by {@code viewer}: another user's edition is {@code 404} (prompt NP-003). */
    public void delete(UUID id, EditionViewer viewer) {
        BriefingEntity entity = load(id);
        if (!visibleTo(entity, viewer)) {
            throw NotFoundException.of("Briefing", id);
        }
        EditionSiteService sites = editionSites == null ? null : editionSites.getIfAvailable();
        if (sites != null) {
            // NP-005: the site's feed key and share link must stop working with the edition.
            sites.revokeAll(id);
        }
        briefings.delete(entity);
        briefings.flush();
    }

    private void apply(BriefingEntity entity, BriefingInput input, UUID ownerTokenId, String ownerScope, Instant now) {
        input.applyTo(entity);
        entity.setOwnerTokenId(ownerTokenId);
        entity.setOwnerScope(ownerScope);
        entity.setUpdatedAt(now);
        entity.setNextRunAt(input.enabled() ? BriefingValidator.nextRun(input.schedule(), input.timezone(), now) : null);
    }

    /** The {@code Briefing} object with its current channel warnings. */
    public Map<String, Object> view(BriefingEntity entity) {
        return BriefingViews.briefing(entity, validator.channelWarnings(BriefingInput.of(entity).channelIds()));
    }

    // ------------------------------------------------------------------------- runs

    /**
     * {@code runBriefing}: runs now, stores and delivers, waiting up to the run timeout.
     *
     * @throws ConflictException {@code BRIEFING_RUNNING} while a run of it is in progress
     */
    public Map<String, Object> runNow(UUID id) {
        BriefingEntity briefing = load(id);
        Instant now = clock.instant();
        if (!claim(id, now)) {
            throw new ConflictException(ErrorCodes.BRIEFING_RUNNING,
                    "A run of briefing " + id + " is in progress; try again when it has finished.");
        }
        Future<BriefingRunEntity> future = executor.submit(() -> execute(briefing));
        try {
            return BriefingViews.run(future.get(properties.runTimeout().toMillis(), TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            return runs.findFirstByBriefingIdOrderByStartedAtDescIdDesc(id)
                    .map(BriefingViews::run)
                    .orElseThrow(() -> new ConflictException(ErrorCodes.BRIEFING_RUNNING,
                            "The run of briefing " + id + " is still starting."));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the briefing run", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("The briefing run failed", cause);
        }
    }

    /**
     * Runs a briefing whose run mark is already claimed, then releases the mark and records the run
     * on the briefing (status, start, next fire time after now).
     */
    BriefingRunEntity execute(BriefingEntity briefing) {
        Instant started = clock.instant();
        String status = BriefingRunEntity.FAILED;
        try {
            BriefingRunEntity run = runner.run(briefing);
            status = run.getStatus();
            if (briefing.isSiteEnabled()) {
                // NP-005: renew the site's keys when they near expiry.
                syncSite(briefing);
            }
            return run;
        } catch (RuntimeException e) {
            log.error("Briefing '{}' ({}) failed", briefing.getName(), briefing.getId(), e);
            throw e;
        } finally {
            finish(briefing.getId(), started, status);
        }
    }

    /**
     * Claims the run mark of {@code id}: true when no run is in progress (or the mark is stale).
     *
     * @throws NotFoundException when the briefing does not exist
     */
    boolean claim(UUID id, Instant now) {
        int claimed = jdbc.update("UPDATE briefing SET running_since = ? WHERE id = ?"
                + " AND (running_since IS NULL OR running_since < ?)", Timestamp.from(now), id,
                Timestamp.from(now.minus(properties.staleRun())));
        if (claimed == 0 && !briefings.existsById(id)) {
            throw NotFoundException.of("Briefing", id);
        }
        return claimed == 1;
    }

    /** Releases the run mark and records the run; {@code next_run_at} becomes the next fire time after now. */
    void finish(UUID id, Instant started, String status) {
        try {
            Optional<BriefingEntity> current = briefings.findById(id);
            if (current.isEmpty()) {
                return;
            }
            BriefingEntity b = current.get();
            Instant next = null;
            if (b.isEnabled()) {
                try {
                    next = BriefingValidator.nextRun(b.getSchedule(), b.getTimezone(), clock.instant());
                } catch (IllegalArgumentException e) {
                    next = null;
                }
            }
            jdbc.update("UPDATE briefing SET running_since = NULL, last_run_at = ?, last_run_status = ?,"
                    + " next_run_at = ? WHERE id = ?", Timestamp.from(started), status,
                    next == null ? null : Timestamp.from(next), id);
        } catch (RuntimeException e) {
            log.error("Could not record the run of briefing {}", id, e);
        }
    }

    /**
     * {@code previewBriefing}: renders without storing, delivering or changing any schedule.
     *
     * @param body the {@code BriefingInput}
     * @param caller who asks (private records are listed only for an admin)
     */
    public Map<String, Object> preview(JsonNode body, McpCaller caller) {
        BriefingValidator.Validated valid = validator.validate(body, caller.excludedSources());
        BriefingInput input = valid.input();
        Instant now = clock.instant();
        BriefingEntity draft = new BriefingEntity(UUID.randomUUID());
        input.applyTo(draft);
        draft.setOwnerUserId(viewer(caller).userId());
        BriefingRunner.Outcome outcome = runner.render(new BriefingRunner.Plan(input.name(), input,
                BriefingRunner.periodFrom(input, null, now), now, runner.hiddenSources(scope(caller)),
                caller.restriction(), runner.edition(draft)));
        Map<String, Object> out = BriefingViews.preview(outcome);
        if (!valid.warnings().isEmpty()) {
            List<String> warnings = new ArrayList<>(outcome.warnings());
            warnings.addAll(valid.warnings());
            out.put("warnings", warnings);
        }
        return out;
    }

    /**
     * {@code listBriefingRuns}: newest first.
     *
     * @param id the briefing (a deleted briefing's runs stay listed while they exist)
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     * @return {@code BriefingRunPage}
     */
    public Map<String, Object> runs(UUID id, String cursor, int limit) {
        return runs(id, cursor, limit, true);
    }

    /**
     * True when {@code run} may hold private-source data (prompt 039): an admin-owned briefing lists
     * private records, so a run whose citations or text name a private source is shown only to callers
     * that can see private sources. Fails closed: a mention of a private source id anywhere counts.
     */
    public boolean citesPrivate(BriefingRunEntity run) {
        if (run == null || visibility == null) {
            return false;
        }
        return citesAny(run, visibility.privateSourceIds());
    }

    /** The private sources (prompt 039); empty without the visibility component. */
    public java.util.Set<String> privateSources() {
        return visibility == null ? java.util.Set.of() : visibility.privateSourceIds();
    }

    /** True when {@code run}'s citations or text name any of {@code sourceIds}. */
    public boolean citesAny(BriefingRunEntity run, java.util.Set<String> sourceIds) {
        java.util.Set<String> privateIds = sourceIds;
        if (run == null || privateIds == null || privateIds.isEmpty()) {
            return false;
        }
        String citations = run.getCitations() == null ? "" : run.getCitations().toString();
        String content = run.getContentMarkdown() == null ? "" : run.getContentMarkdown();
        for (String id : privateIds) {
            if (citations.contains(id) || content.contains(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code listBriefingRuns}; runs that cite a private source are left out unless {@code seesPrivate}
     * (prompt 039).
     */
    public Map<String, Object> runs(UUID id, String cursor, int limit, boolean seesPrivate) {
        if (!briefings.existsById(id) && !runs.existsByBriefingId(id)) {
            throw NotFoundException.of("Briefing", id);
        }
        StringBuilder sql = new StringBuilder("SELECT id FROM briefing_run WHERE briefing_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(id);
        if (cursor != null && !cursor.isBlank()) {
            Position after = decode(cursor);
            sql.append(" AND (started_at < ? OR (started_at = ? AND id < ?))");
            args.add(Timestamp.from(after.startedAt()));
            args.add(Timestamp.from(after.startedAt()));
            args.add(after.id());
        }
        sql.append(" ORDER BY started_at DESC, id DESC LIMIT ?");
        args.add(limit + 1);
        List<UUID> ids = jdbc.queryForList(sql.toString(), UUID.class, args.toArray());
        boolean more = ids.size() > limit;
        List<UUID> page = more ? ids.subList(0, limit) : ids;
        Map<UUID, BriefingRunEntity> loaded = new LinkedHashMap<>();
        runs.findAllById(page).forEach(r -> loaded.put(r.getId(), r));
        List<Map<String, Object>> items = new ArrayList<>();
        BriefingRunEntity last = null;
        for (UUID runId : page) {
            BriefingRunEntity run = loaded.get(runId);
            if (run != null) {
                last = run;
                if (seesPrivate || !citesPrivate(run)) {
                    items.add(BriefingViews.run(run));
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more && last != null ? encode(last) : null);
        return out;
    }

    /**
     * The newest {@code limit} finished runs of a briefing for its Atom and JSON feeds (prompt 058);
     * runs that cite a private source are left out unless {@code seesPrivate} (prompt 039).
     *
     * @throws NotFoundException for an unknown briefing without runs
     */
    public List<BriefingRunEntity> feedRuns(UUID id, int limit, boolean seesPrivate) {
        if (!briefings.existsById(id) && !runs.existsByBriefingId(id)) {
            throw NotFoundException.of("Briefing", id);
        }
        List<BriefingRunEntity> out = new ArrayList<>();
        for (BriefingRunEntity run : runs.findByBriefingIdAndStatusNotOrderByStartedAtDescIdDesc(id,
                BriefingRunEntity.RUNNING, org.springframework.data.domain.PageRequest.of(0, limit))) {
            if (seesPrivate || !citesPrivate(run)) {
                out.add(run);
            }
        }
        return out;
    }

    /**
     * One run of a briefing.
     *
     * @throws NotFoundException when there is no such run of that briefing
     */
    public BriefingRunEntity run(UUID briefingId, UUID runId) {
        return runs.findById(runId).filter(r -> r.getBriefingId().equals(briefingId))
                .orElseThrow(() -> NotFoundException.of("Briefing run", runId));
    }

    /**
     * The latest stored run of a briefing that is no longer running.
     *
     * @throws NotFoundException for an unknown briefing or one without runs
     */
    public BriefingRunEntity latest(UUID briefingId) {
        return runs.findFirstByBriefingIdAndStatusNotOrderByStartedAtDescIdDesc(briefingId, BriefingRunEntity.RUNNING)
                .orElseThrow(() -> briefings.existsById(briefingId)
                        ? new NotFoundException("Briefing '" + briefingId + "' has no runs yet.")
                        : NotFoundException.of("Briefing", briefingId));
    }

    // ------------------------------------------------------------------------- helpers

    private static UUID owner(McpCaller caller) {
        return caller.token() == null ? null : caller.token().id();
    }

    private static String scope(McpCaller caller) {
        return caller.scope() == null ? null : caller.scope().wireValue();
    }

    private record Position(Instant startedAt, UUID id) {}

    private static String encode(BriefingRunEntity last) {
        return encode(last.getStartedAt(), last.getId());
    }

    private static String encode(Instant at, UUID id) {
        String raw = at.getEpochSecond() + "." + at.getNano() + ":" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static Position decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int colon = raw.indexOf(':');
            String[] time = raw.substring(0, colon).split("\\.");
            Instant at = Instant.ofEpochSecond(Long.parseLong(time[0]), Long.parseLong(time[1]));
            return new Position(at, UUID.fromString(raw.substring(colon + 1)));
        } catch (RuntimeException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor from this list")));
        }
    }
}

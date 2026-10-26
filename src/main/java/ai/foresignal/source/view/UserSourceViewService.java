package ai.foresignal.source.view;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.Permissions;
import ai.foresignal.security.user.SessionReadNarrowing;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;

/**
 * The instance collection set and each user's source view over it (prompt R2-010, docs/RBAC-Design.md §D).
 *
 * <ul>
 *   <li><strong>The instance set</strong> ({@code getCollectionSources}/{@code updateCollectionSources},
 *       {@code collection.manage}) is {@code source_settings.enabled}: what the instance is allowed to collect. It is
 *       changed through {@link SourceService#update}, so the credential and parameter checks of {@code updateSource}
 *       apply.
 *   <li><strong>A user's view</strong> ({@code getMySources}/{@code updateMySources}, {@code self.sources}) is the
 *       user's {@code user_source_pref} rows. It is opt-out: a source with no row is in the view. A user may keep a
 *       preference for a source the instance does not collect; it takes effect while the source is in the instance
 *       set.
 *   <li><strong>The union</strong> the collector pulls is {@link CollectionUnion}.
 *   <li><strong>View filtering</strong>: {@link #narrow} removes the sources a signed-in user turned off from the
 *       source member of their read restriction, so every read path that honors a restricted token's sources
 *       (records, changes, tiles, search, stats, the map) shows the user only their view.
 * </ul>
 */
@Service
public class UserSourceViewService implements SessionReadNarrowing {

    /** A display color: {@code #RRGGBB}, the form source descriptors use. */
    static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    /** The longest label override. */
    static final int MAX_DISPLAY_NAME = 200;

    private final UserSourcePrefRepository prefs;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final SourceSettingsRepository settings;
    private final SourceVisibility visibility;
    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** The sources each user turned off, cached until their view changes. */
    private final Map<UUID, Set<String>> hidden = new ConcurrentHashMap<>();

    /**
     * Creates the service.
     *
     * @param prefs the view rows
     * @param registry the registered sources
     * @param sources the source settings service, which changes the instance set
     * @param settings the source settings rows
     * @param visibility private-source visibility
     * @param jdbc the database, for the union counts
     * @param events the event publisher
     * @param clock the clock
     */
    public UserSourceViewService(UserSourcePrefRepository prefs, SourceRegistry registry, SourceService sources,
            SourceSettingsRepository settings, SourceVisibility visibility, JdbcTemplate jdbc,
            ApplicationEventPublisher events, Clock clock) {
        this.prefs = prefs;
        this.registry = registry;
        this.sources = sources;
        this.settings = settings;
        this.visibility = visibility;
        this.jdbc = jdbc;
        this.events = events;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- a user's view

    /**
     * {@code UserSourceView}: every registered source the caller may see, ordered by id, with the caller's preference
     * or the default (enabled, no overrides).
     *
     * @param userId the signed-in user
     * @param token the caller's token, for private-source visibility
     * @return the view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> view(UUID userId, Optional<CurrentToken> token) {
        Map<String, UserSourcePrefEntity> rows = new HashMap<>();
        prefs.findByUserIdOrderBySourceId(userId).forEach(row -> rows.put(row.getSourceId(), row));
        List<Map<String, Object>> out = new ArrayList<>();
        for (String id : new TreeSet<>(registry.ids())) {
            if (!visibility.visible(id, token)) {
                continue;
            }
            UserSourcePrefEntity row = rows.get(id);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sourceId", id);
            item.put("enabled", row == null || row.isEnabled());
            item.put("displayName", row == null ? null : row.getDisplayName());
            item.put("color", row == null ? null : row.getColor());
            out.add(item);
        }
        return Map.of("sources", out);
    }

    /**
     * {@code updateMySources}: replaces the caller's preferences with {@code body.sources}. A source left out returns
     * to the default (in the view, no overrides).
     *
     * @param userId the signed-in user
     * @param token the caller's token, for private-source visibility
     * @param body a {@code UserSourceViewUpdate}
     * @return the new view
     * @throws ValidationException when the body is not a valid update
     */
    @Transactional
    public Map<String, Object> replace(UUID userId, Optional<CurrentToken> token, JsonNode body) {
        List<UserSourcePrefEntity> parsed = parseView(userId, token, body);
        prefs.deleteByUser(userId);
        prefs.flush();
        prefs.saveAllAndFlush(parsed);
        changed(userId);
        return view(userId, token);
    }

    private List<UserSourcePrefEntity> parseView(UUID userId, Optional<CurrentToken> token, JsonNode body) {
        JsonNode list = sourcesArray(body);
        List<FieldError> errors = new ArrayList<>();
        Set<String> seen = new TreeSet<>();
        List<UserSourcePrefEntity> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String at = "sources[" + i + "]";
            JsonNode item = list.get(i);
            if (item == null || !item.isObject()) {
                errors.add(new FieldError(at, "must be an object"));
                continue;
            }
            String id = sourceId(item, at, token, seen, errors);
            JsonNode enabled = item.get("enabled");
            if (enabled == null || !enabled.isBoolean()) {
                errors.add(new FieldError(at + ".enabled", "is required and must be a boolean"));
            }
            String displayName = optionalString(item, "displayName", at, errors);
            if (displayName != null && displayName.length() > MAX_DISPLAY_NAME) {
                errors.add(new FieldError(at + ".displayName", "must be at most " + MAX_DISPLAY_NAME + " characters"));
            }
            String color = optionalString(item, "color", at, errors);
            if (color != null && !COLOR.matcher(color).matches()) {
                errors.add(new FieldError(at + ".color", "must be a color in the form #RRGGBB"));
            }
            if (id != null && enabled != null && enabled.isBoolean()) {
                out.add(new UserSourcePrefEntity(userId, id, enabled.asBoolean(), displayName, color, clock.instant()));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The source view is not valid.", errors);
        }
        return out;
    }

    // ------------------------------------------------------------------------- the instance set

    /**
     * {@code CollectionSet}: every registered source, ordered by id, with whether the instance collects it and how
     * many active users' views enable it.
     *
     * @return the collection set
     */
    @Transactional(readOnly = true)
    public Map<String, Object> collectionSet() {
        Set<String> inCollection = new TreeSet<>();
        settings.findByEnabledTrue().forEach(row -> inCollection.add(row.getSourceId()));
        long active = activeUsers();
        Map<String, Long> disabledBy = disabledCounts();
        List<Map<String, Object>> out = new ArrayList<>();
        for (String id : new TreeSet<>(registry.ids())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sourceId", id);
            item.put("inCollection", inCollection.contains(id));
            item.put("enabledByUsers", active - disabledBy.getOrDefault(id, 0L));
            out.add(item);
        }
        return Map.of("sources", out);
    }

    /**
     * {@code updateCollectionSources}: sets {@code inCollection} for every listed source, all or nothing. A source
     * left out keeps its state; {@code enabledByUsers} is read-only and ignored.
     *
     * @param body a {@code CollectionSetUpdate}
     * @return the new collection set
     * @throws ValidationException when the body is not a valid update
     * @throws ai.foresignal.api.error.ConflictException {@code CREDENTIALS_REQUIRED} when a source would be collected
     *     without the credentials it needs
     */
    @Transactional
    public Map<String, Object> replaceCollection(JsonNode body) {
        JsonNode list = sourcesArray(body);
        List<FieldError> errors = new ArrayList<>();
        Set<String> seen = new TreeSet<>();
        Map<String, Boolean> wanted = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String at = "sources[" + i + "]";
            JsonNode item = list.get(i);
            if (item == null || !item.isObject()) {
                errors.add(new FieldError(at, "must be an object"));
                continue;
            }
            String id = sourceId(item, at, Optional.empty(), seen, errors);
            JsonNode in = item.get("inCollection");
            if (in == null || !in.isBoolean()) {
                errors.add(new FieldError(at + ".inCollection", "is required and must be a boolean"));
            } else if (id != null) {
                wanted.put(id, in.asBoolean());
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The collection set is not valid.", errors);
        }
        wanted.forEach((id, in) -> {
            boolean now = settings.findById(id).map(SourceSettingsEntity::isEnabled).orElse(false);
            if (now != in) {
                sources.update(id, SourceSettingsUpdate.enabled(in));
            }
        });
        return collectionSet();
    }

    // ------------------------------------------------------------------------- view filtering

    /**
     * A signed-in user's read restriction narrowed to their view: the sources they turned off are removed from its
     * source member. Unchanged for a session holding {@code users.manage} or {@code collection.manage} (whoever manages
     * the instance reads the whole collection), and for a user who turned nothing off.
     *
     * @param restriction the restriction so far
     * @param session the session
     * @return the narrowed restriction
     */
    @Override
    public ReadRestriction narrow(ReadRestriction restriction, AuthenticatedSession session) {
        Set<String> keys = session.permissions().keys();
        if (keys.contains(Permissions.USERS_MANAGE.key()) || keys.contains(Permissions.COLLECTION_MANAGE.key())) {
            return restriction;
        }
        Set<String> off = hiddenFor(session.userId());
        if (off.isEmpty()) {
            return restriction;
        }
        Set<String> allowed = new TreeSet<>(restriction.sources() == null ? registry.ids() : restriction.sources());
        allowed.removeAll(off);
        return restriction.withSources(allowed);
    }

    /**
     * The sources {@code userId} turned off.
     *
     * @param userId the user
     * @return the source ids, possibly empty
     */
    public Set<String> hiddenFor(UUID userId) {
        return hidden.computeIfAbsent(userId, id -> Set.copyOf(jdbc.queryForList(
                "select source_id from user_source_pref where user_id = ? and not enabled", String.class, id)));
    }

    /** Drops a user's cached view once a change commits. */
    @EventListener
    public void onViewsChanged(SourceViewsChangedEvent event) {
        hidden.remove(event.userId());
    }

    /**
     * Deletes a user's view (their deletion, R2-005) and announces it after commit.
     *
     * @param userId the user
     * @return how many rows were deleted
     */
    @Transactional
    public int deleteView(UUID userId) {
        int n = prefs.deleteByUser(userId);
        changed(userId);
        return n;
    }

    // ------------------------------------------------------------------------- internals

    private long activeUsers() {
        Long n = jdbc.queryForObject("select count(*) from \"user\" where status = 'active'", Long.class);
        return n == null ? 0 : n;
    }

    private Map<String, Long> disabledCounts() {
        Map<String, Long> out = new HashMap<>();
        jdbc.query("select p.source_id, count(*) from user_source_pref p join \"user\" u on u.id = p.user_id"
                + " where u.status = 'active' and not p.enabled group by p.source_id",
                rs -> {
                    out.put(rs.getString(1), rs.getLong(2));
                });
        return out;
    }

    private static JsonNode sourcesArray(JsonNode body) {
        JsonNode list = body == null ? null : body.get("sources");
        if (list == null || !list.isArray()) {
            throw ValidationException.forField("sources", "is required and must be an array");
        }
        return list;
    }

    private String sourceId(JsonNode item, String at, Optional<CurrentToken> token, Set<String> seen,
            List<FieldError> errors) {
        JsonNode node = item.get("sourceId");
        if (node == null || !node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(at + ".sourceId", "is required"));
            return null;
        }
        String id = node.asString();
        if (!registry.contains(id) || (token.isPresent() && !visibility.visible(id, token))) {
            errors.add(new FieldError(at + ".sourceId", "is not a registered source"));
            return null;
        }
        if (!seen.add(id)) {
            errors.add(new FieldError(at + ".sourceId", "appears more than once"));
            return null;
        }
        return id;
    }

    private static String optionalString(JsonNode item, String field, String at, List<FieldError> errors) {
        JsonNode node = item.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(at + "." + field, "must be a string or null"));
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }

    private void changed(UUID userId) {
        SourceViewsChangedEvent event = new SourceViewsChangedEvent(userId);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            events.publishEvent(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                events.publishEvent(event);
            }
        });
    }
}

package ai.foresignal.settings;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.store.AppSettingEntity;
import ai.foresignal.store.AppSettingRepository;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Typed access to the {@code app_setting} table.
 *
 * <p>Values are stored as {@code jsonb} and converted with the application's Jackson mapper, so a
 * settings record such as {@link RetentionSettings} round-trips through its normal JSON form and
 * shorthand durations stay readable in the database.
 *
 * <p>Reads are served from an in-memory cache. Settings change rarely and are read on hot paths
 * (every scheduler tick consults retention, every request consults CORS), so caching matters; the
 * cache is invalidated on write.
 */
@Service
public class AppSettingService {

    private final AppSettingRepository repository;
    private final JsonMapper jsonMapper;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** key -> deserialized value. Populated on read, cleared for a key on write. */
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public AppSettingService(
            AppSettingRepository repository,
            JsonMapper jsonMapper,
            ApplicationEventPublisher events,
            Clock clock) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
        this.events = events;
        this.clock = clock;
    }

    /** The stored value for {@code key}, or empty when it has never been set. */
    @Transactional(readOnly = true)
    public <T> Optional<T> get(String key, Class<T> type) {
        Object cached = cache.get(key);
        if (cached != null) {
            return Optional.of(type.cast(cached));
        }
        return repository
                .findById(key)
                .map(entity -> deserialize(key, entity.getValue(), type))
                .map(
                        value -> {
                            cache.put(key, value);
                            return value;
                        });
    }

    /**
     * The stored value for {@code key}, or {@code def} when it has never been set.
     *
     * <p>The default is not written back, so a setting the operator has never touched keeps
     * following the shipped default rather than being frozen at today's value.
     */
    @Transactional(readOnly = true)
    public <T> T getOrDefault(String key, Class<T> type, Supplier<T> def) {
        return get(key, type).orElseGet(def);
    }

    /** Stores {@code value} for {@code key} and announces the change once the write commits. */
    @Transactional
    public void put(String key, Object value) {
        String json = serialize(key, value);
        AppSettingEntity entity =
                repository
                        .findById(key)
                        .map(
                                existing -> {
                                    existing.setValue(json);
                                    existing.setUpdatedAt(clock.instant());
                                    return existing;
                                })
                        .orElseGet(() -> new AppSettingEntity(key, json, clock.instant()));
        repository.save(entity);

        // Drop the cache entry now so a read later in this same transaction does not serve the
        // stale value, and publish only once the write is durable.
        cache.remove(key);
        publishAfterCommit(key);
    }

    /**
     * Forgets every cached value. Used by configuration import and by tests.
     *
     * <p>Also announces an {@link AppSettingChangedEvent} for every key in {@link SettingKeys#ALL}.
     * Components such as the CORS and token filters keep their own copy of a setting and drop it
     * only on that event; clearing this cache alone left them serving the old value (checkpoint 012,
     * {@code CorsFilterIT}).
     */
    public void invalidateCache() {
        cache.clear();
        SettingKeys.ALL.forEach(key -> events.publishEvent(new AppSettingChangedEvent(key)));
    }

    private void publishAfterCommit(String key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // No transaction to wait for (for example a direct call in a unit test).
            events.publishEvent(new AppSettingChangedEvent(key));
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        events.publishEvent(new AppSettingChangedEvent(key));
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            // The stored value is not what we cached against; start clean.
                            cache.remove(key);
                        }
                    }
                });
    }

    private String serialize(String key, Object value) {
        if (value == null) {
            throw new InvalidInputException("Setting '" + key + "' cannot be set to null");
        }
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new InvalidInputException(
                    "Cannot serialize setting '" + key + "': " + e.getOriginalMessage(), e);
        }
    }

    private <T> T deserialize(String key, String json, Class<T> type) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Setting '"
                            + key
                            + "' is stored in a form that does not match "
                            + type.getSimpleName()
                            + ": "
                            + e.getOriginalMessage(),
                    e);
        }
    }
}

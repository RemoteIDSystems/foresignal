package ai.foresignal.news;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.NotificationTranslator;
import ai.foresignal.media.outlet.OutletRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * A user's news subscriptions (prompt NP-003; {@code getMyNewsSubscriptions} and {@code updateMyNewsSubscriptions},
 * {@code self.sources}), stored in {@code user_news_subscription}: the outlets (133) and languages the user follows.
 * With no entry of a type there is no restriction of that type; with at least one, the user's editions keep only news
 * from one of the outlets, and in (or translated into, NP-002) one of the languages.
 */
@Service
public class NewsSubscriptionService {

    /** An outlet subscription. */
    public static final String OUTLET = "outlet";

    /** A language subscription. */
    public static final String LANGUAGE = "language";

    /** The most subscriptions a user may hold. */
    public static final int MAX = 500;

    private final JdbcTemplate jdbc;
    private final OutletRegistry outlets;
    private final Clock clock;

    /** Creates the service. */
    public NewsSubscriptionService(JdbcTemplate jdbc, OutletRegistry outlets, Clock clock) {
        this.jdbc = jdbc;
        this.outlets = outlets;
        this.clock = clock;
    }

    /**
     * A user's subscriptions as filters.
     *
     * @param outletIds the outlets followed; empty for no outlet restriction
     * @param languages the canonical languages followed; empty for no language restriction
     */
    public record Filter(Set<String> outletIds, Set<String> languages) {

        /** No restriction. */
        public static final Filter NONE = new Filter(Set.of(), Set.of());

        /** Copies the sets. */
        public Filter {
            outletIds = outletIds == null ? Set.of() : Set.copyOf(outletIds);
            languages = languages == null ? Set.of() : Set.copyOf(languages);
        }

        /** True when neither type restricts. */
        public boolean none() {
            return outletIds.isEmpty() && languages.isEmpty();
        }
    }

    /** The user's subscriptions as filters. */
    public Filter filter(UUID userId) {
        if (userId == null) {
            return Filter.NONE;
        }
        Set<String> outletIds = new LinkedHashSet<>();
        Set<String> languages = new LinkedHashSet<>();
        jdbc.query("SELECT type, value FROM user_news_subscription WHERE user_id = ? ORDER BY type, value", rs -> {
            if (OUTLET.equals(rs.getString(1))) {
                outletIds.add(rs.getString(2));
            } else {
                languages.add(rs.getString(2));
            }
        }, userId);
        return new Filter(outletIds, languages);
    }

    /** {@code getMyNewsSubscriptions}: the {@code NewsSubscriptionView}. */
    public Map<String, Object> view(UUID userId) {
        List<Map<String, Object>> items = new ArrayList<>();
        Instant[] updated = {null};
        jdbc.query("SELECT type, value, updated_at FROM user_news_subscription WHERE user_id = ? ORDER BY type, value",
                rs -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", rs.getString(1));
                    item.put("id", rs.getString(2));
                    items.add(item);
                    Instant at = rs.getTimestamp(3).toInstant();
                    if (updated[0] == null || at.isAfter(updated[0])) {
                        updated[0] = at;
                    }
                }, userId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("subscriptions", items);
        out.put("updatedAt", updated[0] == null ? null : updated[0].toString());
        return out;
    }

    /**
     * {@code updateMyNewsSubscriptions}: replaces the user's subscriptions. An outlet is stored by its canonical id
     * (an id or domain {@link OutletRegistry#find} resolves); a language by its canonical BCP 47 tag.
     *
     * @throws ValidationException listing every problem
     */
    @Transactional
    public Map<String, Object> replace(UUID userId, JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a NewsSubscriptionUpdate object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!"subscriptions".equals(member)) {
                errors.add(new FieldError(member, "is not a NewsSubscriptionUpdate member"));
            }
        }
        JsonNode list = body.get("subscriptions");
        Set<List<String>> rows = new LinkedHashSet<>();
        if (list == null || !list.isArray()) {
            errors.add(new FieldError("subscriptions", "is required and must be an array"));
        } else if (list.size() > MAX) {
            errors.add(new FieldError("subscriptions", "may hold at most " + MAX + " entries"));
        } else {
            int i = 0;
            for (JsonNode entry : list) {
                String at = "subscriptions[" + i++ + "]";
                if (!entry.isObject()) {
                    errors.add(new FieldError(at, "must be an object {type, id}"));
                    continue;
                }
                for (String member : entry.propertyNames()) {
                    if (!"type".equals(member) && !"id".equals(member)) {
                        errors.add(new FieldError(at + "." + member, "is not a NewsSubscription member"));
                    }
                }
                String type = entry.path("type").isString() ? entry.get("type").asString() : null;
                String id = entry.path("id").isString() ? entry.get("id").asString().strip() : null;
                if (!OUTLET.equals(type) && !LANGUAGE.equals(type)) {
                    errors.add(new FieldError(at + ".type", "must be outlet or language"));
                } else if (id == null || id.isEmpty() || id.length() > 200) {
                    errors.add(new FieldError(at + ".id", "must be 1 to 200 characters"));
                } else if (OUTLET.equals(type)) {
                    String resolved = outlets.find(id).map(OutletRegistry.Outlet::id).orElse(null);
                    if (resolved == null) {
                        errors.add(new FieldError(at + ".id", "is not a known media outlet"));
                    } else {
                        rows.add(List.of(OUTLET, resolved));
                    }
                } else {
                    String tag = NotificationTranslator.canonicalTag(id);
                    if (tag == null) {
                        errors.add(new FieldError(at + ".id", "is not a BCP 47 language tag"));
                    } else {
                        rows.add(List.of(LANGUAGE, tag));
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The news subscriptions are not valid.", errors);
        }
        Timestamp now = Timestamp.from(clock.instant());
        jdbc.update("DELETE FROM user_news_subscription WHERE user_id = ?", userId);
        for (List<String> row : rows) {
            jdbc.update("INSERT INTO user_news_subscription (user_id, type, value, updated_at) VALUES (?, ?, ?, ?)",
                    userId, row.get(0), row.get(1), now);
        }
        return view(userId);
    }
}

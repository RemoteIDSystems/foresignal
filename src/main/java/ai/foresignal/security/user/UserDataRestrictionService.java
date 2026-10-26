package ai.foresignal.security.user;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.RestrictionValidator;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;

/**
 * Per-user data restrictions (prompt R2-013): an admin may limit what a user reads to a source and kind subset and an
 * area (bbox, GeoJSON area, places), the data part of an API token's {@code ReadRestriction}, set through
 * {@code updateUser} ({@code dataRestriction}).
 *
 * <p>Nothing here interprets the restriction: as a {@link SessionReadNarrowing} it intersects the session's
 * {@link ReadRestriction} with the stored one ({@link ReadRestriction#intersect}), and every read path already clamps
 * through {@code RestrictionClamp}, exactly as for a restricted token. A user without a row is unrestricted. The
 * restriction applies to every session of the user, an admin's included; an admin lifts it with {@code updateUser}.
 * Lookups are cached per user and dropped when a change commits.
 */
@Service
public class UserDataRestrictionService implements SessionReadNarrowing {

    /** The {@code ReadRestriction} members a user's data restriction may carry. */
    public static final Set<String> MEMBERS = Set.of("sources", "kinds", "bbox", "area", "placeIds");

    /** The request field it is set through. */
    public static final String FIELD = "dataRestriction";

    private final UserDataRestrictionRepository rows;
    private final RestrictionValidator validator;
    private final Clock clock;
    private final ConcurrentMap<UUID, Optional<TokenRestrictions>> cache = new ConcurrentHashMap<>();

    /**
     * Creates the service.
     *
     * @param rows the stored restrictions
     * @param validator the token restriction validator, which checks sources, kinds, bbox, area and places
     * @param clock the clock
     */
    public UserDataRestrictionService(UserDataRestrictionRepository rows, RestrictionValidator validator,
            Clock clock) {
        this.rows = rows;
        this.validator = validator;
        this.clock = clock;
    }

    /**
     * Validates a {@code dataRestriction} member.
     *
     * @param node the member; null or JSON null clears the restriction
     * @return the restriction, or null to clear it
     * @throws ValidationException when a member is unknown or not valid
     */
    public TokenRestrictions parse(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        List<FieldError> errors = new ArrayList<>();
        if (node.isObject()) {
            for (String member : node.propertyNames()) {
                if (!MEMBERS.contains(member)) {
                    errors.add(new FieldError(FIELD + "." + member,
                            "is not a data restriction member (sources, kinds, bbox, area, placeIds)"));
                }
            }
        }
        TokenRestrictions parsed = validator.parse(node, Scope.READ, FIELD, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The data restriction is not valid.", errors);
        }
        return parsed.hasDataRestrictions() ? parsed : null;
    }

    /**
     * The user's stored restriction.
     *
     * @param userId the user
     * @return the restriction, or empty when the user is unrestricted
     */
    public Optional<TokenRestrictions> get(UUID userId) {
        return cache.computeIfAbsent(userId, id -> rows.findById(id).map(UserDataRestrictionEntity::getRestrictions));
    }

    /**
     * Replaces the user's restriction; null (or one without data members) removes it.
     *
     * @param userId the user
     * @param restriction the restriction, or null for none
     */
    @Transactional
    public void set(UUID userId, TokenRestrictions restriction) {
        if (restriction == null || !restriction.hasDataRestrictions()) {
            if (rows.existsById(userId)) {
                rows.deleteById(userId);
                rows.flush();
            }
        } else {
            UserDataRestrictionEntity row = rows.findById(userId)
                    .orElseGet(() -> new UserDataRestrictionEntity(userId, clock.instant()));
            row.setRestrictions(restriction, clock.instant());
            rows.saveAndFlush(row);
        }
        forget(userId);
    }

    /**
     * The {@code UserDataRestriction} view of a user's restriction.
     *
     * @param userId the user
     * @return the view, or null when the user is unrestricted
     */
    public Map<String, Object> view(UUID userId) {
        return get(userId).map(UserDataRestrictionService::view).orElse(null);
    }

    /**
     * The {@code UserDataRestriction} view of {@code r}.
     *
     * @param r the restriction
     * @return its data members
     */
    public static Map<String, Object> view(TokenRestrictions r) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sources", r.sources());
        out.put("kinds", r.kinds());
        out.put("bbox", r.bbox());
        out.put("area", r.area());
        out.put("placeIds", r.placeIds());
        return out;
    }

    @Override
    public ReadRestriction narrow(ReadRestriction restriction, AuthenticatedSession session) {
        Optional<TokenRestrictions> stored = get(session.userId());
        return stored.isEmpty() ? restriction : restriction.intersect(ReadRestriction.of(stored.get(), false));
    }

    private void forget(UUID userId) {
        cache.remove(userId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cache.remove(userId);
                }
            });
        }
    }
}

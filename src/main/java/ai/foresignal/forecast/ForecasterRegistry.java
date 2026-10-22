package ai.foresignal.forecast;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The registered forecasters (prompt 122): the built-in baselines ({@code base-rate}, {@code climatology},
 * {@code persistence}, {@code precursor}, {@code analog}) and the {@code ensemble}, registered at startup with their
 * default configuration, plus one {@code operator:{handle}} forecaster per token principal that submits forecasts,
 * registered on its first forecast. Later prompts register their own ({@code indicator-bayes} in 125,
 * {@code statistical} in 126, {@code impact} in 127, {@code llm:*} in 128, {@code external} in 138) through
 * {@link #ensure}.
 */
@Service
public class ForecasterRegistry {

    /** The base-rate baseline. */
    public static final String BASE_RATE = "base-rate";

    /** The climatology baseline. */
    public static final String CLIMATOLOGY = "climatology";

    /** The persistence baseline. */
    public static final String PERSISTENCE = "persistence";

    /** The precursor baseline. */
    public static final String PRECURSOR = "precursor";

    /** The analog baseline. */
    public static final String ANALOG = "analog";

    /** The threshold-crossing forecaster (prompt 156): a series crossing probability from a predictive distribution. */
    public static final String THRESHOLD = "threshold";

    /** The ensemble. */
    public static final String ENSEMBLE = "ensemble";

    /** The prefix of operator forecasters. */
    public static final String OPERATOR_PREFIX = "operator:";

    /** The version of the built-in forecasters. */
    public static final String BUILT_IN_VERSION = "1.0.0";

    private final ForecasterRepository forecasters;
    private final PrincipalRepository principals;
    private final Clock clock;

    /** Creates the registry. */
    public ForecasterRegistry(ForecasterRepository forecasters, PrincipalRepository principals, Clock clock) {
        this.forecasters = forecasters;
        this.principals = principals;
        this.clock = clock;
    }

    /** The default configuration of each built-in forecaster. */
    public static Map<String, Map<String, Object>> builtIns() {
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        out.put(BASE_RATE, Map.of());
        out.put(CLIMATOLOGY, Map.of("seasonalDays", 14, "maxYears", 30, "minSamples", 5));
        out.put(PERSISTENCE, Map.of("binaryConfidence", 0.9));
        out.put(PRECURSOR, Map.of());
        out.put(ANALOG, Map.of("k", 10));
        out.put(THRESHOLD, Map.of());
        out.put(ENSEMBLE, Map.of("maxAgeDays", 7, "eta", 2.0, "shrink", 20.0, "externalMinResolved", 30));
        return out;
    }

    /** Registers the built-in forecasters that are missing. */
    @EventListener(ApplicationReadyEvent.class)
    public void registerBuiltIns() {
        Map<String, String> names = Map.of(BASE_RATE, "Base rate (outside view)", CLIMATOLOGY, "Climatology",
                PERSISTENCE, "Persistence", PRECURSOR, "Validated precursor", ANALOG, "Historical analogs", THRESHOLD,
                "Threshold crossing", ENSEMBLE, "Ensemble");
        builtIns().forEach((id, config) -> {
            try {
                ensure(id, id, names.get(id), BUILT_IN_VERSION, config);
            } catch (DataIntegrityViolationException e) {
                // Another node registered it first.
            }
        });
    }

    /**
     * The forecaster with {@code id}, registering it with {@code type}, {@code name}, {@code version} and
     * {@code config} when missing. An existing forecaster keeps its configuration and active flag.
     */
    @Transactional
    public ForecasterEntity ensure(String id, String type, String name, String version, Map<String, Object> config) {
        Optional<ForecasterEntity> existing = forecasters.findById(id);
        if (existing.isPresent()) {
            return existing.get();
        }
        ForecasterEntity f = new ForecasterEntity(id, type, name, version, clock.instant());
        f.setConfig(config);
        return forecasters.saveAndFlush(f);
    }

    /** The forecaster with {@code id}, or 404 {@code UNKNOWN_FORECASTER}. */
    public ForecasterEntity get(String id) {
        return find(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_FORECASTER, "No forecaster '"
                + id + "'."));
    }

    /** The forecaster with {@code id}, if registered. */
    public Optional<ForecasterEntity> find(String id) {
        return id == null ? Optional.empty() : forecasters.findById(id);
    }

    /** Every forecaster, by id. */
    public List<ForecasterEntity> all() {
        return forecasters.findAllByOrderByIdAsc();
    }

    /** Sets a forecaster's active flag and, when given, replaces its configuration. */
    @Transactional
    public ForecasterEntity update(String id, boolean active, Map<String, Object> config) {
        return update(id, active, config, null);
    }

    /**
     * Sets a forecaster's active flag and, when given, replaces its configuration and display name (prompt 155:
     * naming an external forecaster).
     */
    @Transactional
    public ForecasterEntity update(String id, boolean active, Map<String, Object> config, String name) {
        ForecasterEntity f = get(id);
        f.setActive(active);
        if (config != null) {
            f.setConfig(config);
        }
        if (name != null && !name.isBlank()) {
            f.setName(name);
        }
        return forecasters.save(f);
    }

    /**
     * Pre-registers (or renames/re-configures) an external forecaster independent of the auto-derived
     * {@code edp:{…}} id (prompt 155): an operator can name it and set a per-forecaster
     * {@code externalMinResolved} before any forecast arrives. An existing forecaster keeps its type and is
     * updated in place.
     */
    @Transactional
    public ForecasterEntity registerExternal(String id, String name, Map<String, Object> config) {
        Optional<ForecasterEntity> existing = forecasters.findById(id);
        if (existing.isPresent()) {
            ForecasterEntity f = existing.get();
            if (name != null && !name.isBlank()) {
                f.setName(name);
            }
            if (config != null) {
                f.setConfig(config);
            }
            return forecasters.save(f);
        }
        ForecasterEntity f = new ForecasterEntity(id, "external", name == null || name.isBlank() ? id : name, "1.0.0",
                clock.instant());
        f.setConfig(config == null ? Map.of() : config);
        return forecasters.saveAndFlush(f);
    }

    /**
     * The id of the operator forecaster of a token: {@code operator:{handle}} of its principal, else
     * {@code operator:} plus the token name made into a slug.
     */
    public String operatorId(AuthenticatedToken token) {
        String handle = null;
        if (token.principalId() != null) {
            handle = principals.findById(token.principalId()).map(PrincipalEntity::getHandle).orElse(null);
        }
        return OPERATOR_PREFIX + slug(handle != null ? handle : token.name());
    }

    /** The operator forecaster of a token, registered on first use. */
    public ForecasterEntity operatorFor(AuthenticatedToken token) {
        String id = operatorId(token);
        return find(id).orElseGet(() -> {
            try {
                return ensure(id, "operator", "Operator " + id.substring(OPERATOR_PREFIX.length()),
                        BUILT_IN_VERSION, Map.of());
            } catch (DataIntegrityViolationException e) {
                return get(id);
            }
        });
    }

    /** A slug of {@code text} that fits the forecaster id grammar after the {@code operator:} prefix. */
    static String slug(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        String slug = lower.replaceAll("[^a-z0-9._:-]+", "-").replaceAll("^[^a-z0-9]+", "")
                .replaceAll("-+$", "");
        if (slug.isEmpty()) {
            slug = "anonymous";
        }
        return slug.length() > 110 ? slug.substring(0, 110) : slug;
    }
}

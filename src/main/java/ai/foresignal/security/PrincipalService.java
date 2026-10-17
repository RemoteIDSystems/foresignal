package ai.foresignal.security;

import ai.foresignal.api.error.ValidationException;
import java.text.Normalizer;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates and resolves {@link PrincipalEntity principals} for tokens (v1.3, prompt 023).
 *
 * <p>023 needs the owner of the bootstrap token, a principal per token created without one, and a
 * lookup by id or handle. Prompt 130 adds the disabled check that {@link TokenService} applies to every
 * authentication; the principal endpoints live in {@code ai.foresignal.collab.PrincipalAdminService}.
 */
@Service
public class PrincipalService {

    /** The OpenAPI {@code Principal.handle} pattern. */
    public static final Pattern HANDLE = Pattern.compile("^[a-z0-9][a-z0-9_.-]{1,31}$");

    private static final int MAX_HANDLE = 32;

    private final PrincipalRepository principals;
    private final Clock clock;
    private final com.github.benmanes.caffeine.cache.Cache<String, Boolean> disabled =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder().maximumSize(10_000)
                    .expireAfterWrite(java.time.Duration.ofSeconds(30)).build();

    /** Creates the service. */
    public PrincipalService(PrincipalRepository principals, Clock clock) {
        this.principals = principals;
        this.clock = clock;
    }

    /** The {@code owner} principal, created when missing (it owns the bootstrap token). */
    @Transactional
    public PrincipalEntity owner() {
        return principals.findByHandle(PrincipalEntity.OWNER_HANDLE)
                .orElseGet(() -> principals.saveAndFlush(new PrincipalEntity(Ulids.next(clock.instant()),
                        PrincipalEntity.OWNER_HANDLE, "Owner", PrincipalKind.PERSON, clock.instant())));
    }

    /**
     * A new principal named after {@code tokenName}, for a token created without one. Its handle is
     * the name in lower case with other characters replaced by {@code -}, made unique with a numeric
     * suffix.
     *
     * @param tokenName the token's name
     * @param kind what the principal is
     * @return the saved principal
     */
    @Transactional
    public PrincipalEntity createFor(String tokenName, PrincipalKind kind) {
        String base = handleFrom(tokenName);
        String handle = base;
        for (int i = 2; principals.existsByHandle(handle); i++) {
            String suffix = "-" + i;
            handle = base.substring(0, Math.min(base.length(), MAX_HANDLE - suffix.length())) + suffix;
        }
        String display = tokenName == null || tokenName.isBlank() ? handle : tokenName.strip();
        if (display.length() > 200) {
            display = display.substring(0, 200);
        }
        return principals.saveAndFlush(new PrincipalEntity(Ulids.next(clock.instant()), handle, display, kind,
                clock.instant()));
    }

    /**
     * The principal with id or handle {@code idOrHandle}.
     *
     * @throws ValidationException when there is none (field {@code principalId})
     */
    @Transactional(readOnly = true)
    public PrincipalEntity resolve(String idOrHandle) {
        Optional<PrincipalEntity> found = Ulids.isUlid(idOrHandle)
                ? principals.findById(idOrHandle)
                : Optional.empty();
        if (found.isEmpty() && idOrHandle != null) {
            found = principals.findByHandle(idOrHandle.strip().toLowerCase(Locale.ROOT));
        }
        return found.orElseThrow(() -> ValidationException.forField("principalId",
                "no principal has the id or handle '" + idOrHandle + "'"));
    }

    /** The principal {@code id}, if it exists. */
    @Transactional(readOnly = true)
    public Optional<PrincipalEntity> find(String id) {
        return id == null ? Optional.empty() : principals.findById(id);
    }

    /**
     * True when principal {@code id} is disabled (prompt 130): its tokens stop working. Cached for 30
     * seconds per principal; {@link #forget} drops the entry when an admin changes the principal.
     */
    public boolean isDisabled(String id) {
        if (id == null) {
            return false;
        }
        return disabled.get(id, key -> principals.findById(key).map(PrincipalEntity::isDisabled).orElse(false));
    }

    /** Drops the cached disabled flag of {@code id}. */
    public void forget(String id) {
        if (id != null) {
            disabled.invalidate(id);
        }
    }

    /** A valid handle derived from {@code name}. */
    static String handleFrom(String name) {
        String ascii = name == null ? "" : Normalizer.normalize(name, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder();
        for (char c : ascii.toCharArray()) {
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '.' || c == '-';
            char next = ok ? c : '-';
            if (next == '-' && (out.isEmpty() || out.charAt(out.length() - 1) == '-')) {
                continue;
            }
            out.append(next);
        }
        while (!out.isEmpty() && !Character.isLetterOrDigit(out.charAt(0))) {
            out.deleteCharAt(0);
        }
        while (!out.isEmpty() && out.charAt(out.length() - 1) == '-') {
            out.deleteCharAt(out.length() - 1);
        }
        String handle = out.length() > MAX_HANDLE ? out.substring(0, MAX_HANDLE) : out.toString();
        while (handle.endsWith("-")) {
            handle = handle.substring(0, handle.length() - 1);
        }
        if (handle.length() < 2) {
            handle = "token" + (handle.isEmpty() ? "" : "-" + handle);
        }
        return handle;
    }
}

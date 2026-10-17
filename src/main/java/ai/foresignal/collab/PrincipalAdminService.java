package ai.foresignal.collab;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Principals (prompt 130): people, agents and services that hold scoped tokens. There are no passwords: a principal
 * authenticates only with its tokens and 023 pairing codes. Admins create and change principals and issue their
 * tokens; anyone with a token lists the public summaries (handle, display name, kind, avatar). A disabled principal's
 * tokens stop working ({@link PrincipalService#isDisabled}).
 */
@Service
public class PrincipalAdminService {

    /** A plausible email address (notification delivery validates it for real). */
    static final Pattern EMAIL = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]{1,255}$");

    private static final Set<String> INPUT = Set.of("handle", "displayName", "kind", "email", "avatarMediaId",
            "timezone", "organization");
    private static final Set<String> PATCH = Set.of("handle", "displayName", "email", "avatarMediaId", "timezone",
            "organization", "disabled");

    private final PrincipalRepository principals;
    private final PrincipalService principalService;
    private final ApiTokenRepository tokens;
    private final TokenService tokenService;
    private final CollabAudit audit;
    private final Clock clock;

    /** Creates the service. */
    public PrincipalAdminService(PrincipalRepository principals, PrincipalService principalService,
            ApiTokenRepository tokens, TokenService tokenService, CollabAudit audit, Clock clock) {
        this.principals = principals;
        this.principalService = principalService;
        this.tokens = tokens;
        this.tokenService = tokenService;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * The principal with id or handle {@code idOrHandle}.
     *
     * @throws NotFoundException {@code UNKNOWN_PRINCIPAL} when there is none
     */
    @Transactional(readOnly = true)
    public PrincipalEntity get(String idOrHandle) {
        return find(idOrHandle).orElseThrow(() -> unknown(idOrHandle));
    }

    /** The principal with id or handle {@code idOrHandle}, if any. */
    @Transactional(readOnly = true)
    public Optional<PrincipalEntity> find(String idOrHandle) {
        if (idOrHandle == null || idOrHandle.isBlank()) {
            return Optional.empty();
        }
        String key = idOrHandle.strip();
        Optional<PrincipalEntity> found = Ulids.isUlid(key) ? principals.findById(key) : Optional.empty();
        return found.isPresent() ? found : principals.findByHandle(key.toLowerCase(Locale.ROOT));
    }

    /** The {@code UNKNOWN_PRINCIPAL} problem. */
    public static NotFoundException unknown(String idOrHandle) {
        return new NotFoundException(ErrorCodes.UNKNOWN_PRINCIPAL, "No principal has the id or handle '" + idOrHandle
                + "'.");
    }

    /**
     * A page of principal summaries by handle ({@code listPrincipals}).
     *
     * @param q a substring of the handle or display name, or null
     * @param kind person, agent or service, or null
     * @param cursor the opaque cursor, or null
     * @param limit the page size
     * @return the {@code PrincipalSummaryPage}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> list(String q, String kind, String cursor, int limit) {
        if (kind != null && !List.of("person", "agent", "service").contains(kind)) {
            throw ValidationException.forField("kind", "must be person, agent or service");
        }
        String needle = q == null || q.isBlank() ? null : q.strip().toLowerCase(Locale.ROOT);
        List<PrincipalEntity> all = new ArrayList<>(principals.findAll());
        all.sort(Comparator.comparing(PrincipalEntity::getHandle));
        List<Map<String, Object>> items = new ArrayList<>();
        int offset = CollabInputs.offset(cursor);
        int seen = 0;
        boolean more = false;
        for (PrincipalEntity p : all) {
            if (kind != null && !kind.equals(p.getKind())) {
                continue;
            }
            if (needle != null && !p.getHandle().contains(needle)
                    && !p.getDisplayName().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            if (seen++ < offset) {
                continue;
            }
            if (items.size() == limit) {
                more = true;
                break;
            }
            items.add(PrincipalViews.summary(p));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** Active tokens of a principal. */
    @Transactional(readOnly = true)
    public int tokenCount(String principalId) {
        Instant now = clock.instant();
        return (int) tokens.findByPrincipalIdOrderByCreatedAtDesc(principalId).stream()
                .filter(t -> !t.isPathKey() && t.isActive(now)).count();
    }

    /**
     * Creates a principal ({@code createPrincipal}, admin).
     *
     * @throws ConflictException {@code PRINCIPAL_HANDLE_TAKEN} when the handle is in use
     */
    @Transactional
    public PrincipalEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "PrincipalInput", INPUT);
        String handle = CollabInputs.required(body, "handle", 2, 32);
        if (!PrincipalService.HANDLE.matcher(handle).matches()) {
            throw ValidationException.forField("handle", "must match ^[a-z0-9][a-z0-9_.-]{1,31}$");
        }
        String displayName = CollabInputs.required(body, "displayName", 1, 200).strip();
        String kindText = CollabInputs.required(body, "kind", 1, 16);
        PrincipalKind kind;
        try {
            kind = PrincipalKind.valueOf(kindText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("kind", "must be person, agent or service");
        }
        if (principals.existsByHandle(handle)) {
            throw new ConflictException(ErrorCodes.PRINCIPAL_HANDLE_TAKEN, "The handle '" + handle
                    + "' is already in use.");
        }
        Instant now = clock.instant();
        PrincipalEntity p = new PrincipalEntity(Ulids.next(now), handle, displayName, kind, now);
        applyProfile(p, body, now);
        principals.saveAndFlush(p);
        audit.record(caller, "createPrincipal", CollabAudit.TARGET_PRINCIPAL, p.getId(), Map.of("handle", handle,
                "kind", kind.wireValue()));
        return p;
    }

    /** Applies a JSON merge patch ({@code updatePrincipal}, admin); the handle is immutable. */
    @Transactional
    public PrincipalEntity patch(String idOrHandle, JsonNode body, ObjectCaller caller) {
        PrincipalEntity p = get(idOrHandle);
        CollabInputs.object(body, "PrincipalPatch", PATCH);
        if (body.has("handle") && !p.getHandle().equals(body.get("handle").asString(""))) {
            throw ValidationException.forField("handle", "is immutable");
        }
        Instant now = clock.instant();
        if (body.has("displayName")) {
            p.setDisplayName(CollabInputs.required(body, "displayName", 1, 200).strip(), now);
        }
        applyProfile(p, body, now);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("fields", new ArrayList<>(body.propertyNames()));
        if (body.has("disabled")) {
            boolean disabled = CollabInputs.bool(body, "disabled", false);
            p.setDisabled(disabled, now);
            detail.put("disabled", disabled);
        }
        principals.saveAndFlush(p);
        principalService.forget(p.getId());
        audit.record(caller, "updatePrincipal", CollabAudit.TARGET_PRINCIPAL, p.getId(), detail);
        return p;
    }

    /**
     * Issues a token for a principal ({@code createPrincipalToken}, admin): an {@code ApiTokenInput} whose
     * {@code principalId} is forced to this principal.
     */
    @Transactional
    public TokenService.CreatedToken issueToken(String idOrHandle, JsonNode body, ObjectCaller caller) {
        PrincipalEntity p = get(idOrHandle);
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an ApiTokenInput object.");
        }
        ObjectNode input = ((ObjectNode) body).deepCopy();
        input.put("principalId", p.getId());
        TokenService.CreatedToken created = tokenService.create(input);
        audit.record(caller, "createPrincipalToken", CollabAudit.TARGET_PRINCIPAL, p.getId(), Map.of("tokenId",
                created.entity().getId().toString(), "scope", created.entity().getScope().wireValue()));
        return created;
    }

    /** The active tokens of a principal. */
    @Transactional(readOnly = true)
    public List<ApiTokenEntity> tokens(String principalId) {
        Instant now = clock.instant();
        return tokens.findByPrincipalIdOrderByCreatedAtDesc(principalId).stream()
                .filter(t -> !t.isPathKey() && t.isActive(now)).toList();
    }

    private static void applyProfile(PrincipalEntity p, JsonNode body, Instant now) {
        if (body.has("email")) {
            String email = CollabInputs.text(body, "email", 3, 320);
            if (email != null && !EMAIL.matcher(email.strip()).matches()) {
                throw ValidationException.forField("email", "must be an email address");
            }
            p.setEmail(email == null ? null : email.strip(), now);
        }
        if (body.has("avatarMediaId")) {
            p.setAvatarMediaId(CollabInputs.text(body, "avatarMediaId", 1, 80), now);
        }
        if (body.has("timezone")) {
            String tz = CollabInputs.text(body, "timezone", 1, 64);
            if (tz != null) {
                try {
                    ZoneId.of(tz);
                } catch (DateTimeException e) {
                    throw ValidationException.forField("timezone", "must be an IANA timezone, e.g. Europe/London");
                }
            }
            p.setTimezone(tz, now);
        }
        if (body.has("organization")) {
            String org = CollabInputs.text(body, "organization", 0, 200);
            p.setOrganization(org == null || org.isBlank() ? null : org.strip(), now);
        }
    }

    /** True when two principals belong to the same organization (both without one count as the same). */
    public static boolean sameOrganization(PrincipalEntity a, PrincipalEntity b) {
        return normalize(a == null ? null : a.getOrganization()).equals(normalize(b == null ? null
                : b.getOrganization()));
    }

    /** The organization of a principal, normalized for comparison (empty when none). */
    public static String normalize(String organization) {
        return organization == null ? "" : organization.strip().toLowerCase(Locale.ROOT);
    }
}

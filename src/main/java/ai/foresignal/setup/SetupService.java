package ai.foresignal.setup;

import ai.foresignal.api.AuthController;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionSettingsService;
import ai.foresignal.security.user.SessionService;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.settings.ResourceThresholds;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.settings.SmtpSettingsService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * First-run setup ({@code runSetup}, prompt R2-027): creates the first admin, moves the v1 operator's configuration to
 * them ({@link CutoverMigrator}), seeds the optional SMTP settings, collection sources, resource thresholds and
 * retention, and signs the admin in.
 *
 * <p>All of it is one transaction: a body that fails anywhere (a bad email, a weak password, an unknown source, an SMTP
 * or threshold value the settings refuse) leaves the instance exactly as it was, still in first run. The optional parts
 * are applied with the same services, and so the same validation, as their own settings operations. The caller (the
 * bootstrap admin token) is checked by {@code SetupController}.
 */
@Service
public class SetupService {

    private static final Set<String> FIELDS = Set.of("admin", "smtp", "sources", "thresholds", "retention");
    private static final Set<String> ADMIN_FIELDS = Set.of("email", "fullName", "password");

    private final UserAdminService userAdmin;
    private final SessionService sessions;
    private final CutoverMigrator cutover;
    private final SmtpSettingsService smtp;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final ResourceThresholdsService thresholds;
    private final RetentionSettingsService retention;

    /**
     * Creates the service.
     *
     * @param userAdmin creates the first admin
     * @param sessions signs the admin in
     * @param cutover moves the v1 operator's configuration
     * @param smtp the account email settings
     * @param registry the known sources
     * @param sources the collection set
     * @param thresholds the resource thresholds
     * @param retention the retention settings
     */
    public SetupService(UserAdminService userAdmin, SessionService sessions, CutoverMigrator cutover,
            SmtpSettingsService smtp, SourceRegistry registry, SourceService sources,
            ResourceThresholdsService thresholds, RetentionSettingsService retention) {
        this.userAdmin = userAdmin;
        this.sessions = sessions;
        this.cutover = cutover;
        this.smtp = smtp;
        this.registry = registry;
        this.sources = sources;
        this.thresholds = thresholds;
        this.retention = retention;
    }

    /**
     * Runs setup.
     *
     * @param body the {@code SetupRequest}
     * @param userAgent the caller's {@code User-Agent}, recorded on the session
     * @param remoteIp the caller's address
     * @return the {@code SetupResult}: the new admin's {@code User} and a {@code SessionToken}
     * @throws ValidationException {@code 400} for a body that is not valid
     * @throws ai.foresignal.api.error.ConflictException {@code 409 ALREADY_SETUP} once a user exists
     */
    @Transactional
    public Map<String, Object> run(JsonNode body, String userAgent, String remoteIp) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a SetupRequest object.");
        }
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                throw ValidationException.forField(field, "is not a SetupRequest member");
            }
        }
        JsonNode admin = body.get("admin");
        if (admin == null || !admin.isObject()) {
            throw ValidationException.forField("admin", "is required: an object with email, fullName and password");
        }
        for (String field : admin.propertyNames()) {
            if (!ADMIN_FIELDS.contains(field)) {
                throw ValidationException.forField("admin." + field, "is not a member of admin");
            }
        }
        List<String> sourceIds = sourceIds(body.get("sources"));
        JsonNode thresholdsBody = present(body.get("thresholds"));
        ResourceThresholds parsedThresholds = thresholdsBody == null ? null
                : ResourceThresholdsService.parse(thresholdsBody);
        String password = text(admin, "password");

        UserEntity user = userAdmin.createFirstAdmin(text(admin, "email"), text(admin, "fullName"), password,
                remoteIp);
        cutover.migrate(user.getId());

        JsonNode smtpBody = present(body.get("smtp"));
        if (smtpBody != null) {
            smtp.update(smtpBody);
        }
        for (String id : sourceIds) {
            sources.update(id, SourceSettingsUpdate.enabled(true));
        }
        if (parsedThresholds != null) {
            thresholds.put(parsedThresholds);
        }
        JsonNode retentionBody = present(body.get("retention"));
        if (retentionBody != null) {
            retention.update(retentionBody);
        }

        SessionService.Login login = sessions.login(user.getEmail(), password, null, userAgent, remoteIp);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("user", userAdmin.get(user.getId()));
        out.put("session", login.mfaRequired() || login.session() == null ? null : AuthController.sessionToken(login));
        return out;
    }

    private List<String> sourceIds(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw ValidationException.forField("sources", "must be an array of source ids");
        }
        Set<String> ids = new LinkedHashSet<>();
        List<String> unknown = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            if (item == null || !item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField("sources[" + i + "]", "must be a source id");
            }
            String id = item.asString().strip();
            if (!registry.contains(id)) {
                unknown.add(id);
            }
            ids.add(id);
        }
        if (!unknown.isEmpty()) {
            throw ValidationException.forField("sources", "names unknown sources: " + String.join(", ", unknown));
        }
        return List.copyOf(ids);
    }

    private static JsonNode present(JsonNode node) {
        return node == null || node.isNull() ? null : node;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || !value.isString() ? null : value.asString();
    }
}

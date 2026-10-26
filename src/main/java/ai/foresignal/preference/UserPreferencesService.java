package ai.foresignal.preference;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.PrincipalNotificationEntity;
import ai.foresignal.collab.PrincipalNotificationRepository;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.NotificationSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A signed-in user's preferences ({@code getMyPreferences} / {@code updateMyPreferences}, prompt R2-012, OpenAPI
 * {@code UserPreferences}). Each member has exactly one home, so nothing forks:
 *
 * <ul>
 *   <li>{@code timezone} is the user's principal's timezone ({@code principal.timezone}), the one
 *       {@code patchPrincipal} sets and the user's personal notification channels render quiet hours in. Changing it
 *       here also moves those channels to it.
 *   <li>{@code locale}, {@code theme} and {@code ui} are the {@code user_preferences} row.
 *   <li>{@code notifications} is a summary of, and a set of switches over, the user's existing notification
 *       preferences ({@code principal_notification} and the channels they own, {@code replaceMyNotificationPreferences}):
 *       {@code emailEnabled} covers the email channels, {@code pushEnabled} the push channels ({@link #PUSH_TYPES}),
 *       {@code quietHours} the window every preference shares. Writing a switch writes those rows.
 * </ul>
 *
 * <p>Reads return effective values: an unset timezone reads as the server's, an unset theme as {@code system}, an
 * unset locale as the instance notification language ({@code notify.defaultLanguage}).
 */
@Service
public class UserPreferencesService {

    /** The themes {@code UserPreferences.theme} accepts. */
    public static final List<String> THEMES = List.of("system", "light", "dark");

    /** The theme an unset preference reads as. */
    public static final String DEFAULT_THEME = "system";

    /** The channel types {@code notifications.pushEnabled} switches. */
    public static final Set<String> PUSH_TYPES = Set.of("ntfy", "gotify", "pushover", "webpush");

    /** The channel type {@code notifications.emailEnabled} switches. */
    public static final String EMAIL_TYPE = "email";

    /** Largest serialized {@code ui} bag, in bytes. */
    public static final int MAX_UI_BYTES = 64 * 1024;

    /** {@code HH:MM-HH:MM}. */
    static final Pattern QUIET_HOURS = Pattern.compile("^(([01]\\d|2[0-3]):[0-5]\\d)-(([01]\\d|2[0-3]):[0-5]\\d)$");

    private static final Set<String> FIELDS = Set.of("timezone", "locale", "theme", "notifications", "ui");
    private static final Set<String> NOTIFICATION_FIELDS = Set.of("emailEnabled", "pushEnabled", "quietHours");
    private static final Set<String> ISO_LANGUAGES = Set.of(Locale.getISOLanguages());
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final UserPreferencesRepository stored;
    private final PrincipalRepository principals;
    private final PrincipalNotificationRepository notificationPrefs;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final AppSettingService settings;
    private final UserZones zones;
    private final JsonMapper json;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param stored the {@code user_preferences} rows
     * @param principals the principals, which hold each user's timezone
     * @param notificationPrefs the users' notification preferences
     * @param channels the channel rows those preferences own
     * @param channelService the channel service, which applies a switch to a channel
     * @param settings the instance settings, for the default locale
     * @param zones the timezone resolver, for the default timezone
     * @param json the JSON mapper
     * @param clock the clock
     */
    public UserPreferencesService(UserPreferencesRepository stored, PrincipalRepository principals,
            PrincipalNotificationRepository notificationPrefs, NotificationChannelRepository channels,
            ChannelService channelService, AppSettingService settings, UserZones zones, JsonMapper json, Clock clock) {
        this.stored = stored;
        this.principals = principals;
        this.notificationPrefs = notificationPrefs;
        this.channels = channels;
        this.channelService = channelService;
        this.settings = settings;
        this.zones = zones;
        this.json = json;
        this.clock = clock;
    }

    /**
     * The caller's preferences ({@code getMyPreferences}), with the defaults filled in.
     *
     * @param session the signed-in user
     * @return a {@code UserPreferences} view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> get(AuthenticatedSession session) {
        return view(session.userId(), principal(session));
    }

    /**
     * Replaces the caller's preferences ({@code updateMyPreferences}). {@code timezone}, {@code locale},
     * {@code theme} and {@code ui} are replaced (absent or null resets to the default). {@code notifications} switches
     * the existing notification preferences, so a switch left out (or the whole object left out) keeps them as
     * they are rather than re-enabling channels the user turned off.
     *
     * @param session the signed-in user
     * @param body a {@code UserPreferences} object
     * @return the preferences after the change
     * @throws ValidationException when a member is not valid
     */
    @Transactional
    public Map<String, Object> update(AuthenticatedSession session, JsonNode body) {
        PrincipalEntity principal = principal(session);
        Input input = parse(body);
        Instant now = clock.instant();
        UserPreferencesEntity row = stored.findById(session.userId())
                .orElseGet(() -> new UserPreferencesEntity(session.userId(), now));
        row.setLocale(input.locale());
        row.setTheme(input.theme());
        row.setUi(input.ui());
        row.setUpdatedAt(now);
        stored.save(row);
        if (!Objects.equals(principal.getTimezone(), input.timezone())) {
            principal.setTimezone(input.timezone(), now);
            principals.save(principal);
            for (PrincipalNotificationEntity pref : prefs(principal.getId())) {
                channelService.adjustPersonal(pref.getChannelId(), principal.getId(), null, false, null, true,
                        input.timezone());
            }
        }
        applyNotifications(principal.getId(), input, now);
        return view(session.userId(), principal);
    }

    /**
     * Deletes a user's stored preferences (user deletion, {@link UserPreferencesReassigner}).
     *
     * @param userId the user
     * @return 1 when a row was deleted, else 0
     */
    @Transactional
    public int delete(UUID userId) {
        if (!stored.existsById(userId)) {
            return 0;
        }
        stored.deleteById(userId);
        stored.flush();
        return 1;
    }

    // ------------------------------------------------------------------------- reading

    private Map<String, Object> view(UUID userId, PrincipalEntity principal) {
        UserPreferencesEntity row = stored.findById(userId).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        String timezone = principal.getTimezone();
        out.put("timezone", timezone != null ? timezone : zones.serverZone().getId());
        out.put("locale", row != null && row.getLocale() != null ? row.getLocale() : defaultLocale());
        out.put("theme", row != null && row.getTheme() != null ? row.getTheme() : DEFAULT_THEME);
        out.put("notifications", notifications(principal.getId()));
        out.put("ui", row == null ? new LinkedHashMap<>() : row.getUi());
        return out;
    }

    /**
     * The notification summary: a switch is on unless the user has channels of its kind and every one of them is
     * off; {@code quietHours} is the window every preference shares, or null when they differ or have none.
     */
    Map<String, Object> notifications(String principalId) {
        List<PrincipalNotificationEntity> rows = prefs(principalId);
        Boolean anyEmail = null;
        Boolean anyPush = null;
        String quiet = null;
        boolean first = true;
        boolean sameQuiet = !rows.isEmpty();
        for (PrincipalNotificationEntity pref : rows) {
            String type = channels.findById(pref.getChannelId()).map(NotificationChannelEntity::getType).orElse(null);
            if (EMAIL_TYPE.equals(type)) {
                anyEmail = Boolean.TRUE.equals(anyEmail) || pref.isEnabled();
            } else if (type != null && PUSH_TYPES.contains(type)) {
                anyPush = Boolean.TRUE.equals(anyPush) || pref.isEnabled();
            }
            String window = window(pref.getQuietHours());
            if (first) {
                quiet = window;
                first = false;
            } else if (!Objects.equals(quiet, window)) {
                sameQuiet = false;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("emailEnabled", anyEmail == null || anyEmail);
        out.put("pushEnabled", anyPush == null || anyPush);
        out.put("quietHours", sameQuiet ? quiet : null);
        return out;
    }

    /** {@code HH:MM-HH:MM} of a stored {@code QuietHours}, or null for none. */
    static String window(Map<String, Object> quietHours) {
        if (quietHours == null || !(quietHours.get("start") instanceof String start)
                || !(quietHours.get("end") instanceof String end)) {
            return null;
        }
        return start + "-" + end;
    }

    private String defaultLocale() {
        try {
            return settings.getOrDefault(SettingKeys.NOTIFICATIONS, NotificationSettings.class,
                    NotificationSettings::defaults).defaultLanguage();
        } catch (RuntimeException e) {
            return NotificationSettings.DEFAULT_LANGUAGE;
        }
    }

    // ------------------------------------------------------------------------- writing

    private void applyNotifications(String principalId, Input input, Instant now) {
        if (input.emailEnabled() == null && input.pushEnabled() == null && !input.setQuietHours()) {
            return;
        }
        Map<String, Object> quiet = input.quietHours() == null ? null : quietHours(input.quietHours());
        for (PrincipalNotificationEntity pref : prefs(principalId)) {
            String type = channels.findById(pref.getChannelId()).map(NotificationChannelEntity::getType).orElse(null);
            Boolean enabled = null;
            if (EMAIL_TYPE.equals(type)) {
                enabled = input.emailEnabled();
            } else if (type != null && PUSH_TYPES.contains(type)) {
                enabled = input.pushEnabled();
            }
            if (enabled == null && !input.setQuietHours()) {
                continue;
            }
            if (enabled != null) {
                pref.setEnabled(enabled);
            }
            if (input.setQuietHours()) {
                pref.setQuietHours(quiet == null ? null : new LinkedHashMap<>(quiet));
            }
            pref.setUpdatedAt(now);
            notificationPrefs.save(pref);
            channelService.adjustPersonal(pref.getChannelId(), principalId, enabled, input.setQuietHours(),
                    quiet == null ? null : new LinkedHashMap<>(quiet), false, null);
        }
    }

    /** A {@code QuietHours} object for {@code HH:MM-HH:MM}, every day, with the default bypass. */
    static Map<String, Object> quietHours(String window) {
        Matcher m = QUIET_HOURS.matcher(window);
        if (!m.matches()) {
            throw ValidationException.forField("notifications.quietHours", "must be HH:MM-HH:MM, e.g. 22:00-07:00");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("start", m.group(1));
        out.put("end", m.group(3));
        return out;
    }

    private List<PrincipalNotificationEntity> prefs(String principalId) {
        return new ArrayList<>(notificationPrefs.findByPrincipalIdOrderByCreatedAtAscIdAsc(principalId));
    }

    private PrincipalEntity principal(AuthenticatedSession session) {
        return principals.findById(session.principalId())
                .orElseThrow(() -> NotFoundException.of("Principal", session.principalId()));
    }

    // ------------------------------------------------------------------------- parsing

    /**
     * A validated {@code UserPreferences} body.
     *
     * @param timezone the IANA zone, or null for the server's
     * @param locale the canonical BCP 47 tag, or null for the default
     * @param theme the theme, or null for {@code system}
     * @param ui the UI bag, never null
     * @param emailEnabled the email switch, or null to leave it
     * @param pushEnabled the push switch, or null to leave it
     * @param setQuietHours true when {@code notifications.quietHours} was given
     * @param quietHours the window, or null to clear it
     */
    record Input(String timezone, String locale, String theme, Map<String, Object> ui, Boolean emailEnabled,
            Boolean pushEnabled, boolean setQuietHours, String quietHours) {}

    /** Validates a {@code UserPreferences} body. */
    Input parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a UserPreferences object.");
        }
        for (String name : body.propertyNames()) {
            if (!FIELDS.contains(name)) {
                throw ValidationException.forField(name, "is not a UserPreferences member");
            }
        }
        String timezone = timezone(body.get("timezone"));
        String locale = locale(body.get("locale"));
        String theme = theme(body.get("theme"));
        Map<String, Object> ui = ui(body.get("ui"));
        JsonNode n = body.get("notifications");
        Boolean email = null;
        Boolean push = null;
        boolean setQuiet = false;
        String quiet = null;
        if (n != null && !n.isNull()) {
            if (!n.isObject()) {
                throw ValidationException.forField("notifications", "must be an object");
            }
            for (String name : n.propertyNames()) {
                if (!NOTIFICATION_FIELDS.contains(name)) {
                    throw ValidationException.forField("notifications." + name, "is not a notifications member");
                }
            }
            email = bool(n, "emailEnabled");
            push = bool(n, "pushEnabled");
            if (n.has("quietHours")) {
                setQuiet = true;
                JsonNode q = n.get("quietHours");
                if (q != null && !q.isNull()) {
                    if (!q.isString()) {
                        throw ValidationException.forField("notifications.quietHours", "must be a string or null");
                    }
                    quiet = q.asString();
                    quietHours(quiet);
                }
            }
        }
        return new Input(timezone, locale, theme, ui, email, push, setQuiet, quiet);
    }

    private static String timezone(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }
        if (!n.isString() || !ZoneId.getAvailableZoneIds().contains(n.asString())) {
            throw ValidationException.forField("timezone", "must be an IANA timezone, e.g. Europe/London");
        }
        return n.asString();
    }

    private static String locale(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }
        if (!n.isString() || n.asString().isBlank() || n.asString().length() > 35) {
            throw ValidationException.forField("locale", "must be a BCP 47 language tag, e.g. en-US");
        }
        Locale locale;
        try {
            locale = new Locale.Builder().setLanguageTag(n.asString()).build();
        } catch (IllformedLocaleException e) {
            throw ValidationException.forField("locale", "must be a BCP 47 language tag, e.g. en-US");
        }
        if (!ISO_LANGUAGES.contains(locale.getLanguage())) {
            throw ValidationException.forField("locale", "must name an ISO 639 language, e.g. en-US");
        }
        return locale.toLanguageTag();
    }

    private static String theme(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }
        if (!n.isString() || !THEMES.contains(n.asString())) {
            throw ValidationException.forField("theme", "must be one of " + THEMES);
        }
        return n.asString();
    }

    private Map<String, Object> ui(JsonNode n) {
        if (n == null || n.isNull()) {
            return new LinkedHashMap<>();
        }
        if (!n.isObject()) {
            throw ValidationException.forField("ui", "must be an object");
        }
        if (json.writeValueAsBytes(n).length > MAX_UI_BYTES) {
            throw ValidationException.forField("ui", "must be at most " + MAX_UI_BYTES + " bytes of JSON");
        }
        return json.convertValue(n, MAP);
    }

    private static Boolean bool(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        if (!v.isBoolean()) {
            throw ValidationException.forField("notifications." + field, "must be a boolean");
        }
        return v.asBoolean();
    }
}

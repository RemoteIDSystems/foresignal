package ai.foresignal.media.outlet;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The declared media-rating providers (prompt 133): what each one is (code: name, license, egress, default state) and
 * the operator's settings ({@code media_provider}: enabled, the admin's license acknowledgement, a recorded
 * redistribution permission, the encrypted MBFC key, the last import).
 *
 * <ul>
 *   <li>{@code wikidata} (CC0 1.0, on): country, owner, parent, instance-of and political alignment (P1387);
 *   <li>{@code partisan-web} (CC BY 4.0, on): the license file of {@code github.com/stockHuman/tpw-data} was confirmed
 *       to be CC BY 4.0 on 2026-10-17 (the README badge says CC BY 4.0 while its link points to CC BY-SA 4.0);
 *   <li>{@code allsides} (off): ratings the operator imports from their own copy; license reported as CC BY-NC 4.0 but
 *       not confirmed on allsides.com (the site refused automated reads on 2026-10-17); {@code LOCAL_ONLY} unless the
 *       operator records permission;
 *   <li>{@code mbfc} (off): Media Bias/Fact Check, a paid service through the operator's own key; always
 *       {@code LOCAL_ONLY} (its RapidAPI terms exclude redistribution);
 *   <li>{@code operator} (on): the operator's CSV/JSON imports, each row declaring its license.
 * </ul>
 *
 * <p>Enabling a provider whose license an admin has not acknowledged is {@code 409 PROVIDER_LICENCE_NOT_ACKNOWLEDGED};
 * the two open providers start acknowledged by default.
 */
@Component
public class MediaProviders {

    /**
     * What a provider is.
     *
     * @param id the provider id
     * @param name the display name
     * @param defaultEnabled whether it starts enabled
     * @param license the license text
     * @param licenseUrl the license or terms link, or null
     * @param attribution the attribution its license asks for
     * @param egress the egress of its ratings without a recorded permission
     * @param permissionLiftsLocalOnly true when an operator-recorded permission makes its ratings {@code OPEN}
     * @param acceptsKey true when it takes an operator API key
     */
    public record Info(String id, String name, boolean defaultEnabled, String license, String licenseUrl,
            String attribution, String egress, boolean permissionLiftsLocalOnly, boolean acceptsKey) {}

    /** Every provider, in the spec's order. */
    public static final List<Info> INFOS = List.of(
            new Info(MediaVocabulary.WIKIDATA, "Wikidata", true, "CC0 1.0",
                    "https://creativecommons.org/publicdomain/zero/1.0/", "Wikidata (CC0)", MediaVocabulary.OPEN, false,
                    false),
            new Info(MediaVocabulary.PARTISAN_WEB, "The Partisan Web", true, "CC BY 4.0",
                    "https://github.com/stockHuman/tpw-data/blob/master/LICENSE",
                    "The Partisan Web (github.com/stockHuman/tpw-data), CC BY 4.0", MediaVocabulary.OPEN, false,
                    false),
            new Info(MediaVocabulary.ALLSIDES, "AllSides media bias ratings", false,
                    "CC BY-NC 4.0 (reported; confirm on allsides.com)", "https://www.allsides.com/",
                    "AllSides Media Bias Ratings by AllSides.com", MediaVocabulary.LOCAL_ONLY, true, false),
            new Info(MediaVocabulary.MBFC, "Media Bias/Fact Check", false,
                    "Media Bias/Fact Check API terms (paid; the operator's own key)",
                    "https://mediabiasfactcheck.com/mbfcs-data-api", "Media Bias/Fact Check",
                    MediaVocabulary.LOCAL_ONLY, false, true),
            new Info(MediaVocabulary.OPERATOR, "Operator imports", true, "declared per row", null,
                    "declared per row", MediaVocabulary.OPEN, false, false));

    /**
     * A provider with its settings.
     *
     * @param info what it is
     * @param enabled whether it is enabled
     * @param licenseAcknowledged whether an admin acknowledged its license
     * @param acknowledgedBy who acknowledged, or null
     * @param acknowledgedAt when, or null
     * @param permissionRecorded whether the operator recorded redistribution permission
     * @param credentialSet whether an API key is stored
     * @param lastImportAt the last import, or null
     * @param lastImportStatus {@code ok}, {@code partial} or {@code failed}, or null
     * @param lastImportMessage the last import's message, or null
     * @param ratingsCount the provider's stored ratings
     */
    public record State(Info info, boolean enabled, boolean licenseAcknowledged, String acknowledgedBy,
            Instant acknowledgedAt, boolean permissionRecorded, boolean credentialSet, Instant lastImportAt,
            String lastImportStatus, String lastImportMessage, int ratingsCount) {

        /** The egress of this provider's ratings now: {@code OPEN} when its default is, or a permission lifts it. */
        public String egress() {
            if (MediaVocabulary.OPEN.equals(info.egress())) {
                return MediaVocabulary.OPEN;
            }
            return info.permissionLiftsLocalOnly() && permissionRecorded ? MediaVocabulary.OPEN
                    : MediaVocabulary.LOCAL_ONLY;
        }

        /** The {@code MediaProvider} view. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id", info.id());
            out.put("name", info.name());
            out.put("enabled", enabled);
            out.put("defaultEnabled", info.defaultEnabled());
            out.put("licence", info.license());
            out.put("licenceUrl", info.licenseUrl());
            out.put("attribution", info.attribution());
            out.put("licenceAcknowledged", licenseAcknowledged);
            out.put("acknowledgedBy", acknowledgedBy);
            out.put("acknowledgedAt", acknowledgedAt == null ? null : acknowledgedAt.toString());
            out.put("egress", egress());
            out.put("permissionRecorded", permissionRecorded);
            out.put("credentialSet", credentialSet);
            out.put("lastImportAt", lastImportAt == null ? null : lastImportAt.toString());
            out.put("lastImportStatus", lastImportStatus);
            out.put("ratingsCount", ratingsCount);
            return out;
        }
    }

    /**
     * An update ({@code MediaProviderInput}); null members leave the setting as it is.
     *
     * @param enabled enable or disable
     * @param licenseAcknowledged acknowledge (or withdraw the acknowledgement of) the license
     * @param permissionRecorded record (or withdraw) redistribution permission
     * @param apiKey the MBFC key (write-only); blank removes it
     */
    public record Input(Boolean enabled, Boolean licenseAcknowledged, Boolean permissionRecorded, String apiKey) {}

    /** Who acknowledged the open providers by default. */
    static final String DEFAULT_ACKNOWLEDGER = "default (license verified 2026-10-17)";

    private final JdbcTemplate jdbc;
    private final CredentialCipher cipher;
    private final Clock clock;

    /** Creates the component. */
    public MediaProviders(JdbcTemplate jdbc, CredentialCipher cipher, Clock clock) {
        this.jdbc = jdbc;
        this.cipher = cipher;
        this.clock = clock;
    }

    /** The provider info of {@code id}, or null. */
    public static Info info(String id) {
        for (Info i : INFOS) {
            if (i.id().equals(id)) {
                return i;
            }
        }
        return null;
    }

    /** Every provider with its settings. */
    public List<State> list() {
        ensureRows();
        List<State> out = new ArrayList<>();
        for (Info info : INFOS) {
            out.add(read(info));
        }
        return out;
    }

    /**
     * One provider.
     *
     * @throws NotFoundException 404 for an unknown provider
     */
    public State get(String id) {
        Info info = info(id);
        if (info == null) {
            throw new NotFoundException("No media-rating provider '" + id + "'.");
        }
        ensureRows();
        return read(info);
    }

    /** The ids of the enabled providers. */
    public Set<String> enabled() {
        Set<String> out = new TreeSet<>();
        for (State s : list()) {
            if (s.enabled()) {
                out.add(s.info().id());
            }
        }
        return out;
    }

    /**
     * Updates a provider.
     *
     * @param actor who acts (recorded with an acknowledgement)
     * @return the provider after the change
     * @throws ConflictException 409 {@code PROVIDER_LICENCE_NOT_ACKNOWLEDGED} when it would be enabled without an
     *     acknowledged license
     * @throws ValidationException 400 for a permission or key the provider does not take
     */
    public State update(String id, Input input, String actor) {
        State current = get(id);
        Info info = current.info();
        Input in = input == null ? new Input(null, null, null, null) : input;
        List<FieldError> errors = new ArrayList<>();
        if (in.permissionRecorded() != null && in.permissionRecorded() && !info.permissionLiftsLocalOnly()) {
            errors.add(new FieldError("permissionRecorded", "this provider's ratings stay LOCAL_ONLY or are already"
                    + " OPEN; a recorded permission changes nothing"));
        }
        if (in.apiKey() != null && !info.acceptsKey()) {
            errors.add(new FieldError("apiKey", "only mbfc takes an API key"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The provider update is not valid.", errors);
        }
        boolean acknowledged = in.licenseAcknowledged() != null ? in.licenseAcknowledged()
                : current.licenseAcknowledged();
        boolean enabled = in.enabled() != null ? in.enabled() : current.enabled() && acknowledged;
        if (enabled && !acknowledged) {
            throw new ConflictException(ErrorCodes.PROVIDER_LICENCE_NOT_ACKNOWLEDGED, "The license of '" + id
                    + "' (" + info.license() + ") must be acknowledged by an admin before it is enabled.");
        }
        Instant now = clock.instant();
        boolean newlyAcknowledged = acknowledged && !current.licenseAcknowledged();
        jdbc.update("UPDATE media_provider SET enabled = ?, licence_acknowledged = ?, acknowledged_by = ?,"
                        + " acknowledged_at = ?, permission_recorded = ?, updated_at = ? WHERE id = ?",
                enabled, acknowledged, acknowledged ? (newlyAcknowledged ? actor : current.acknowledgedBy()) : null,
                acknowledged ? ts(newlyAcknowledged ? now : current.acknowledgedAt()) : null,
                in.permissionRecorded() != null ? in.permissionRecorded() : current.permissionRecorded(),
                Timestamp.from(now), id);
        if (in.apiKey() != null) {
            if (in.apiKey().isBlank()) {
                jdbc.update("UPDATE media_provider SET api_key_ciphertext = NULL, api_key_nonce = NULL WHERE id = ?",
                        id);
            } else {
                Encrypted e = cipher.encrypt(in.apiKey().strip());
                jdbc.update("UPDATE media_provider SET api_key_ciphertext = ?, api_key_nonce = ? WHERE id = ?",
                        e.ciphertext(), e.nonce(), id);
            }
        }
        return get(id);
    }

    /** The decrypted API key of a provider, or null. */
    public String apiKey(String id) {
        ensureRows();
        List<Encrypted> rows = jdbc.query("SELECT api_key_ciphertext, api_key_nonce FROM media_provider WHERE id = ?",
                (rs, i) -> rs.getBytes(1) == null || rs.getBytes(2) == null ? null
                        : new Encrypted(rs.getBytes(1), rs.getBytes(2)), id);
        return rows.isEmpty() || rows.get(0) == null ? null : cipher.decryptToString(rows.get(0));
    }

    /** Records an import's outcome. */
    public void recordImport(String id, String status, String message, Instant at) {
        ensureRows();
        jdbc.update("UPDATE media_provider SET last_import_at = ?, last_import_status = ?, last_import_message = ?"
                + " WHERE id = ?", Timestamp.from(at), status, message == null ? null
                : message.length() > 1000 ? message.substring(0, 1000) : message, id);
    }

    /** Creates the missing provider rows with their defaults. */
    public void ensureRows() {
        Instant now = clock.instant();
        for (Info info : INFOS) {
            boolean ack = info.defaultEnabled();
            jdbc.update("INSERT INTO media_provider (id, enabled, licence_acknowledged, acknowledged_by, acknowledged_at,"
                            + " permission_recorded, updated_at) VALUES (?, ?, ?, ?, ?, false, ?)"
                            + " ON CONFLICT (id) DO NOTHING", info.id(), info.defaultEnabled(), ack,
                    ack ? DEFAULT_ACKNOWLEDGER : null, ack ? Timestamp.from(now) : null, Timestamp.from(now));
        }
    }

    private State read(Info info) {
        List<State> rows = jdbc.query("SELECT p.enabled, p.licence_acknowledged, p.acknowledged_by, p.acknowledged_at,"
                        + " p.permission_recorded, p.api_key_ciphertext IS NOT NULL AS credential, p.last_import_at,"
                        + " p.last_import_status, p.last_import_message,"
                        + " (SELECT count(*) FROM media_rating r WHERE r.provider = p.id) AS ratings"
                        + " FROM media_provider p WHERE p.id = ?",
                (rs, i) -> new State(info, rs.getBoolean(1), rs.getBoolean(2), rs.getString(3), instant(rs.getObject(4,
                        OffsetDateTime.class)), rs.getBoolean(5), rs.getBoolean(6), instant(rs.getObject(7,
                        OffsetDateTime.class)), rs.getString(8), rs.getString(9), rs.getInt(10)), info.id());
        if (rows.isEmpty()) {
            return new State(info, info.defaultEnabled(), info.defaultEnabled(), null, null, false, false, null, null,
                    null, 0);
        }
        return rows.get(0);
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}

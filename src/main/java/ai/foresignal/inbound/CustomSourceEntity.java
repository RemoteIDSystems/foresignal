package ai.foresignal.inbound;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A runtime-defined push source ({@code custom_source}, Architecture §7.1 and §16, prompt 024).
 *
 * <p>The row is the source of truth for the dynamic descriptor that
 * {@link CustomSourceDescriptors#toDescriptor} builds; external (EDP) providers also have a row
 * here, holding their ingest key and caps, while their descriptor comes from the stored manifest.
 * The ingest key is stored only as its SHA-256 and an 8-character prefix; verifier secrets are
 * encrypted with {@code CredentialCipher}.
 */
@Entity
@Table(name = "custom_source", indexes = @Index(name = "custom_source_key_hash_idx", columnList = "ingest_key_hash",
        unique = true))
public class CustomSourceEntity {

    /** {@code origin} of a source created through {@code /v1/custom-sources}. */
    public static final String ORIGIN_CUSTOM = "custom";

    /** {@code origin} of an external (EDP) provider. */
    public static final String ORIGIN_EXTERNAL = "external";

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin = ORIGIN_CUSTOM;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kinds", nullable = false, columnDefinition = "jsonb")
    private List<String> kinds = new ArrayList<>();

    @Column(name = "attribution", length = 500)
    private String attribution;

    /** ISO-8601 or shorthand duration; null means records never expire. */
    @Column(name = "record_ttl", length = 32)
    private String recordTtl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tile_properties", columnDefinition = "jsonb")
    private List<String> tileProperties = new ArrayList<>();

    /** Null means the descriptor default search fields. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "search_fields", columnDefinition = "jsonb")
    private List<String> searchFields;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "embed_fields", columnDefinition = "jsonb")
    private List<String> embedFields = new ArrayList<>();

    @Column(name = "rank_field", length = 64)
    private String rankField;

    @Column(name = "rate_limit_per_minute", nullable = false)
    private int rateLimitPerMinute = 60;

    @Column(name = "observed_at_mode", length = 16, nullable = false)
    private String observedAtMode = "UPSTREAM";

    @Column(name = "ttl_anchor", length = 16, nullable = false)
    private String ttlAnchor = "RECEIVED";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "volatile_fields", columnDefinition = "jsonb")
    private List<String> volatileFields = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "significant_fields", columnDefinition = "jsonb")
    private List<String> significantFields = new ArrayList<>();

    @Column(name = "title_field", length = 64)
    private String titleField;

    /** The {@code fieldSpecs} object as raw JSON text; null for none. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_specs", columnDefinition = "jsonb")
    private String fieldSpecs;

    @Column(name = "private_source", nullable = false)
    private boolean privateSource;

    @Column(name = "licence", length = 200)
    private String licence;

    /** The structured licence terms as JSON (prompt 109), or null. */
    @Column(name = "licence_terms", columnDefinition = "text")
    private String licenceTerms;

    @Column(name = "records_per_minute", nullable = false)
    private int recordsPerMinute = 6000;

    /** Per-format options as raw JSON text ({@code {"alertmanager": {...}}}); null for none. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "formats", columnDefinition = "jsonb")
    private String formats;

    /** The non-secret part of the verifiers per format as raw JSON text; null for none. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verifiers", columnDefinition = "jsonb")
    private String verifiers;

    /** The verifier secrets per format, a JSON object encrypted with {@code CredentialCipher}. */
    @Column(name = "verifier_secret_ciphertext")
    private byte[] verifierSecretCiphertext;

    @Column(name = "verifier_secret_nonce")
    private byte[] verifierSecretNonce;

    /** The applied preset ({@code personal-location}, prompt 039), or null. */
    @Column(name = "profile", length = 32)
    private String profile;

    /** True when the ingest key may be carried in the path (prompt 039); null reads as false. */
    @Column(name = "allow_path_key")
    private Boolean allowPathKey;

    /** The sender allowlist for path-key and GET pushes (prompt 039); null = private ranges only. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingest_allow_cidrs", columnDefinition = "jsonb")
    private List<String> ingestAllowCidrs;

    /** The {@code RecordMapping} as raw JSON text (prompt 059); null for none. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mapping", columnDefinition = "jsonb")
    private String mapping;

    /** The format {@code POST /v1/ingest/{sourceId}} uses (prompt 059, a preset's format); null = native. */
    @Column(name = "default_format", length = 32)
    private String defaultFormat;

    /** The preset the source was created from (prompt 059), or null. */
    @Column(name = "preset", length = 32)
    private String preset;

    /** Until when raw requests are captured for {@code /samples} (prompt 059); null or past = off. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "capture_samples_until")
    private Instant captureSamplesUntil;

    @Column(name = "ingest_key_hash", length = 64, nullable = false)
    private String ingestKeyHash;

    @Column(name = "ingest_key_prefix", length = 8, nullable = false)
    private String ingestKeyPrefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_ingest_at")
    private Instant lastIngestAt;

    /** For JPA. */
    protected CustomSourceEntity() {}

    /** A new row for {@code sourceId}. */
    public CustomSourceEntity(String sourceId) {
        this.sourceId = sourceId;
    }

    /** The source id. */
    public String getSourceId() {
        return sourceId;
    }

    /** {@link #ORIGIN_CUSTOM} or {@link #ORIGIN_EXTERNAL}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /** True for an external (EDP) provider. */
    public boolean isExternal() {
        return ORIGIN_EXTERNAL.equals(origin);
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Sets the display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The declared kinds. */
    public List<String> getKinds() {
        return kinds;
    }

    /** Sets the kinds. */
    public void setKinds(List<String> kinds) {
        this.kinds = kinds == null ? new ArrayList<>() : new ArrayList<>(kinds);
    }

    /** The attribution line, or null. */
    public String getAttribution() {
        return attribution;
    }

    /** Sets the attribution line. */
    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    /** The record TTL as text, or null. */
    public String getRecordTtl() {
        return recordTtl;
    }

    /** Sets the record TTL text. */
    public void setRecordTtl(String recordTtl) {
        this.recordTtl = recordTtl;
    }

    /** The tile properties. */
    public List<String> getTileProperties() {
        return tileProperties;
    }

    /** Sets the tile properties. */
    public void setTileProperties(List<String> tileProperties) {
        this.tileProperties = copy(tileProperties);
    }

    /** The search fields, or null for the defaults. */
    public List<String> getSearchFields() {
        return searchFields;
    }

    /** Sets the search fields; null for the defaults. */
    public void setSearchFields(List<String> searchFields) {
        this.searchFields = searchFields == null ? null : new ArrayList<>(searchFields);
    }

    /** The embed fields. */
    public List<String> getEmbedFields() {
        return embedFields;
    }

    /** Sets the embed fields. */
    public void setEmbedFields(List<String> embedFields) {
        this.embedFields = copy(embedFields);
    }

    /** The rank field, or null. */
    public String getRankField() {
        return rankField;
    }

    /** Sets the rank field. */
    public void setRankField(String rankField) {
        this.rankField = rankField;
    }

    /** Requests per minute (the budget's per-minute value). */
    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    /** Sets the request rate limit. */
    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    /** {@code UPSTREAM} or {@code RECEIVED}. */
    public String getObservedAtMode() {
        return observedAtMode;
    }

    /** Sets the time base. */
    public void setObservedAtMode(String observedAtMode) {
        this.observedAtMode = observedAtMode;
    }

    /** {@code RECEIVED} or {@code OBSERVED}. */
    public String getTtlAnchor() {
        return ttlAnchor;
    }

    /** Sets the TTL anchor. */
    public void setTtlAnchor(String ttlAnchor) {
        this.ttlAnchor = ttlAnchor;
    }

    /** The volatile fields. */
    public List<String> getVolatileFields() {
        return volatileFields;
    }

    /** Sets the volatile fields. */
    public void setVolatileFields(List<String> volatileFields) {
        this.volatileFields = copy(volatileFields);
    }

    /** The significant fields. */
    public List<String> getSignificantFields() {
        return significantFields;
    }

    /** Sets the significant fields. */
    public void setSignificantFields(List<String> significantFields) {
        this.significantFields = copy(significantFields);
    }

    /** The title field, or null. */
    public String getTitleField() {
        return titleField;
    }

    /** Sets the title field. */
    public void setTitleField(String titleField) {
        this.titleField = titleField;
    }

    /** The field specs as raw JSON, or null. */
    public String getFieldSpecs() {
        return fieldSpecs;
    }

    /** Sets the field specs JSON. */
    public void setFieldSpecs(String fieldSpecs) {
        this.fieldSpecs = fieldSpecs;
    }

    /** True for personal data. */
    public boolean isPrivateSource() {
        return privateSource;
    }

    /** Sets the private flag. */
    public void setPrivateSource(boolean privateSource) {
        this.privateSource = privateSource;
    }

    /** The data license, or null. */
    public String getLicence() {
        return licence;
    }

    /** Sets the data license. */
    public void setLicence(String licence) {
        this.licence = licence;
    }

    /** The structured licence terms as JSON (prompt 109), or null. */
    public String getLicenceTerms() {
        return licenceTerms;
    }

    /** Sets the structured licence terms JSON. */
    public void setLicenceTerms(String licenceTerms) {
        this.licenceTerms = licenceTerms;
    }

    /** The records-per-minute cap. */
    public int getRecordsPerMinute() {
        return recordsPerMinute;
    }

    /** Sets the records-per-minute cap. */
    public void setRecordsPerMinute(int recordsPerMinute) {
        this.recordsPerMinute = recordsPerMinute;
    }

    /** The per-format options as raw JSON, or null. */
    public String getFormats() {
        return formats;
    }

    /** Sets the per-format options JSON. */
    public void setFormats(String formats) {
        this.formats = formats;
    }

    /** The verifier configuration as raw JSON, or null. */
    public String getVerifiers() {
        return verifiers;
    }

    /** Sets the verifier configuration JSON. */
    public void setVerifiers(String verifiers) {
        this.verifiers = verifiers;
    }

    /** The encrypted verifier secrets, or null. */
    public byte[] getVerifierSecretCiphertext() {
        return verifierSecretCiphertext;
    }

    /** The nonce of the encrypted verifier secrets, or null. */
    public byte[] getVerifierSecretNonce() {
        return verifierSecretNonce;
    }

    /** Sets the encrypted verifier secrets; both null for none. */
    public void setVerifierSecret(byte[] ciphertext, byte[] nonce) {
        this.verifierSecretCiphertext = ciphertext;
        this.verifierSecretNonce = nonce;
    }

    /** The SHA-256 of the ingest key. */
    public String getIngestKeyHash() {
        return ingestKeyHash;
    }

    /** The first 8 characters of the ingest key. */
    public String getIngestKeyPrefix() {
        return ingestKeyPrefix;
    }

    /** Stores a new key's hash and prefix. */
    public void setIngestKey(String hash, String prefix) {
        this.ingestKeyHash = hash;
        this.ingestKeyPrefix = prefix;
    }

    /** When the source was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the creation time. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the update time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** When a push last succeeded, or null. */
    public Instant getLastIngestAt() {
        return lastIngestAt;
    }

    /** Sets the last push time. */
    public void setLastIngestAt(Instant lastIngestAt) {
        this.lastIngestAt = lastIngestAt;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    /** The applied preset ({@code personal-location}), or null (prompt 039). */
    public String getProfile() {
        return profile;
    }

    /** Sets {@link #getProfile()}. */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /** True when the source's ingest key may be carried in the path (prompt 039). */
    public boolean isAllowPathKey() {
        return Boolean.TRUE.equals(allowPathKey);
    }

    /** Sets {@link #isAllowPathKey()}. */
    public void setAllowPathKey(boolean allowPathKey) {
        this.allowPathKey = allowPathKey;
    }

    /**
     * The sender allowlist (CIDR blocks) for path-key and GET pushes, or null for private ranges only
     * (prompt 039).
     */
    public List<String> getIngestAllowCidrs() {
        return ingestAllowCidrs == null ? null : List.copyOf(ingestAllowCidrs);
    }

    /** Sets {@link #getIngestAllowCidrs()}; null restores the private-ranges default. */
    public void setIngestAllowCidrs(List<String> ingestAllowCidrs) {
        this.ingestAllowCidrs = ingestAllowCidrs == null ? null : new ArrayList<>(ingestAllowCidrs);
    }

    /** The {@code RecordMapping} JSON text (prompt 059), or null. */
    public String getMapping() {
        return mapping;
    }

    /** Sets the {@code RecordMapping} JSON text. */
    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    /** The format of {@code POST /v1/ingest/{sourceId}} (prompt 059), or null for {@code native}. */
    public String getDefaultFormat() {
        return defaultFormat;
    }

    /** Sets the default format. */
    public void setDefaultFormat(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    /** The preset the source was created from (prompt 059), or null. */
    public String getPreset() {
        return preset;
    }

    /** Sets the preset id. */
    public void setPreset(String preset) {
        this.preset = preset;
    }

    /** Until when samples are captured (prompt 059), or null. */
    public Instant getCaptureSamplesUntil() {
        return captureSamplesUntil;
    }

    /** Sets until when samples are captured; null turns capture off. */
    public void setCaptureSamplesUntil(Instant captureSamplesUntil) {
        this.captureSamplesUntil = captureSamplesUntil;
    }

    /** True while sample capture is on at {@code now}. */
    public boolean capturesSamplesAt(Instant now) {
        return captureSamplesUntil != null && captureSamplesUntil.isAfter(now);
    }
}

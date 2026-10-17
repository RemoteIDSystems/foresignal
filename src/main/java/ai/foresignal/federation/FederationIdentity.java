package ai.foresignal.federation;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.ApiVersion;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * This instance's federation identity (prompt 113): the instance id is the install id (003), the display name is
 * the operator's {@code federation.instanceName}: the stored setting of that key, else the property
 * {@code foresignal.federation.instance-name} (environment {@code FORESIGNAL_FEDERATION_INSTANCE_NAME}), else the
 * {@code instance.name} setting; and the
 * 4-byte short id carried in compact messages (CBOR key 10) is the first four bytes of the SHA-256 of the instance
 * id.
 */
@Component
public class FederationIdentity {

    /** The capabilities {@code /v1/federation/info} advertises. */
    public static final List<String> CAPABILITIES = List.of("changes", "stream", "snapshot", "ingest");

    private final InstallIdService installId;
    private final AppSettingService settings;
    private final ForesignalVersion version;
    private final ApiVersion apiVersion;
    private final ProductUrls urls;
    private final Clock clock;
    private final String configuredName;

    /** Creates the identity. */
    public FederationIdentity(InstallIdService installId, AppSettingService settings, ForesignalVersion version,
            ApiVersion apiVersion, ProductUrls urls, Clock clock,
            @org.springframework.beans.factory.annotation.Value("${foresignal.federation.instance-name:}")
            String configuredName) {
        this.installId = installId;
        this.settings = settings;
        this.version = version;
        this.apiVersion = apiVersion;
        this.urls = urls;
        this.clock = clock;
        this.configuredName = configuredName == null || configuredName.isBlank() ? null : configuredName.strip();
    }

    /** The instance id: the install id. */
    public String instanceId() {
        return installId.installId();
    }

    /** The display name, or null when none is set. */
    public String instanceName() {
        String name = settings.get(SettingKeys.FEDERATION_INSTANCE_NAME, String.class).orElse(null);
        if (name == null || name.isBlank()) {
            name = configuredName;
        }
        if (name == null || name.isBlank()) {
            name = settings.get(SettingKeys.INSTANCE_NAME, String.class).orElse(null);
        }
        return name == null || name.isBlank() ? null : name.strip();
    }

    /** The 4-byte short id of this instance (compact messages, key 10). */
    public byte[] shortId() {
        return shortId(instanceId());
    }

    /** The 4-byte short id of {@code instanceId}: the first four bytes of its SHA-256. */
    public static byte[] shortId(String instanceId) {
        return java.util.Arrays.copyOf(RecordRef.sha256(instanceId == null ? "" : instanceId), 4);
    }

    /** The global reference of a local record. */
    public String ref(String sourceId, String externalId) {
        return RecordRef.format(instanceId(), sourceId, externalId);
    }

    /**
     * The {@code FederationInfo} document: instance id and name, versions, the JWKS URI (absolute when the public
     * base URL or the request is known), the capabilities and the server time.
     *
     * @param request the request, for the origin when no public base URL is configured; may be null
     */
    public Map<String, Object> info(HttpServletRequest request) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("instanceId", instanceId());
        out.put("instanceName", instanceName());
        out.put("version", version.version());
        out.put("apiVersion", apiVersion.version());
        String base = request == null ? urls.storedBase() : urls.base(request);
        out.put("jwksUri", base + "/.well-known/jwks.json");
        out.put("capabilities", CAPABILITIES);
        out.put("time", clock.instant().toString());
        return out;
    }

    /** The UTF-8 bytes of {@code text}. */
    static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}

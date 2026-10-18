package ai.foresignal.chronicle.network;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * The discovery salts of the chronicle network (prompt 134), kept with the instance's secret material: encrypted
 * with {@link CredentialCipher} in {@code chronicle_salt}, never exported and never returned by the API.
 *
 * <ul>
 *   <li>The <b>instance salt</b> keys local discovery. It is 32 random bytes generated on first use and rotated
 *       yearly; a rotation makes every local key set be rebuilt under the new salt.</li>
 *   <li>A <b>network salt</b> keys federated discovery with the peers of one network. Admins agree its value out of
 *       band and install it with {@code PUT /v1/chronicle-network/salts} while {@code network.federated} is true. It is
 *       used for 90 days; after that federated discovery for the network stops until a new salt is installed, so
 *       long-term linkage needs continued consent.</li>
 * </ul>
 */
@Component
public class NetworkSalts {

    /** The network id pattern of {@code NetworkSaltInput}. */
    static final Pattern NETWORK_ID = Pattern.compile("^[a-z0-9][a-z0-9-]{2,63}$");

    private static final Set<String> INPUT_FIELDS = Set.of("networkId", "saltRef");

    private final ChronicleSaltRepository salts;
    private final CredentialCipher cipher;
    private final AppSettingService settings;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    /** Creates the component. */
    public NetworkSalts(ChronicleSaltRepository salts, CredentialCipher cipher, AppSettingService settings,
            Clock clock) {
        this.salts = salts;
        this.cipher = cipher;
        this.settings = settings;
        this.clock = clock;
    }

    /**
     * A usable salt.
     *
     * @param id its id
     * @param kind {@code instance} or {@code network}
     * @param networkId the network, or null
     * @param value the decrypted value
     * @param createdAt when it was created
     * @param rotatesAt when it is due for rotation
     */
    public record Salt(String id, String kind, String networkId, byte[] value, Instant createdAt, Instant rotatesAt) {

        /** A hasher keyed with this salt. */
        public KeyedHasher hasher() {
            return new KeyedHasher(value);
        }

        /** True for a network salt. */
        public boolean network() {
            return ChronicleSaltEntity.NETWORK.equals(kind);
        }
    }

    /** True when federated discovery is on ({@code network.federated}, default false). */
    public boolean federated() {
        try {
            return settings.get(SettingKeys.NETWORK_FEDERATED, Boolean.class).orElse(false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** The minimum estimated shared keys of a link ({@code network.minSharedKeys}, default 5, at least 1). */
    public int minSharedKeys() {
        try {
            return Math.max(1, settings.get(SettingKeys.NETWORK_MIN_SHARED_KEYS, Integer.class)
                    .orElse(NetworkVocabulary.DEFAULT_MIN_SHARED_KEYS));
        } catch (RuntimeException e) {
            return NetworkVocabulary.DEFAULT_MIN_SHARED_KEYS;
        }
    }

    /**
     * The current instance salt, generated on first use and rotated when due.
     *
     * @return the salt
     */
    @Transactional
    public synchronized Salt instanceSalt() {
        Instant now = clock.instant();
        List<ChronicleSaltEntity> current = salts.findByKindAndRetiredAtIsNullOrderByCreatedAtDesc(
                ChronicleSaltEntity.INSTANCE);
        for (ChronicleSaltEntity s : current) {
            if (s.current(now)) {
                return decrypt(s);
            }
        }
        for (ChronicleSaltEntity s : current) {
            s.retire(now);
            salts.save(s);
        }
        byte[] value = new byte[32];
        random.nextBytes(value);
        Encrypted e = cipher.encrypt(value);
        ChronicleSaltEntity row = new ChronicleSaltEntity("inst-" + Ulids.next(now), ChronicleSaltEntity.INSTANCE,
                null, e.ciphertext(), e.nonce(), now, now.plus(NetworkVocabulary.INSTANCE_SALT_ROTATION));
        salts.save(row);
        return decrypt(row);
    }

    /** The ids of every retired instance salt (their key sets are rebuilt under the current one). */
    public List<String> retiredInstanceSalts() {
        List<String> out = new ArrayList<>();
        for (ChronicleSaltEntity s : salts.findAll()) {
            if (ChronicleSaltEntity.INSTANCE.equals(s.getKind()) && s.getRetiredAt() != null) {
                out.add(s.getId());
            }
        }
        return out;
    }

    /** The current salt of a network, if one is installed and not past its 90 days. */
    public Optional<Salt> networkSalt(String networkId) {
        Instant now = clock.instant();
        for (ChronicleSaltEntity s : salts.findByKindAndNetworkIdAndRetiredAtIsNullOrderByCreatedAtDesc(
                ChronicleSaltEntity.NETWORK, networkId)) {
            if (s.current(now)) {
                return Optional.of(decrypt(s));
            }
        }
        return Optional.empty();
    }

    /** Every current network salt (none while federated discovery is off). */
    public List<Salt> networkSalts() {
        if (!federated()) {
            return List.of();
        }
        Instant now = clock.instant();
        List<Salt> out = new ArrayList<>();
        for (ChronicleSaltEntity s : salts.findByKindAndRetiredAtIsNullOrderByCreatedAtDesc(
                ChronicleSaltEntity.NETWORK)) {
            if (s.current(now)) {
                out.add(decrypt(s));
            }
        }
        return out;
    }

    /** The ids of network salts that are retired or past their rotation date (their state is dropped). */
    public List<String> staleNetworkSalts() {
        Instant now = clock.instant();
        List<String> out = new ArrayList<>();
        for (ChronicleSaltEntity s : salts.findAll()) {
            if (ChronicleSaltEntity.NETWORK.equals(s.getKind()) && !s.current(now)) {
                out.add(s.getId());
            }
        }
        return out;
    }

    /**
     * Installs a network salt ({@code setChronicleNetworkSalt}, admin) and retires the network's previous one.
     *
     * @param body the {@code NetworkSaltInput}
     * @return the installed salt and the ids of the salts it replaced
     * @throws ConflictException {@code 409 FEDERATED_DISCOVERY_DISABLED} while {@code network.federated} is false
     */
    @Transactional
    public Installed install(JsonNode body) {
        CollabInputs.object(body, "NetworkSaltInput", INPUT_FIELDS);
        String networkId = CollabInputs.required(body, "networkId", 3, 64).strip();
        if (!NETWORK_ID.matcher(networkId).matches()) {
            throw ValidationException.forField("networkId", "must match ^[a-z0-9][a-z0-9-]{2,63}$");
        }
        JsonNode ref = body.get("saltRef");
        if (ref == null || !ref.isString() || ref.asString().length() < 16) {
            throw ValidationException.forField("saltRef", "is required: the salt agreed with the peers, at least 16"
                    + " characters");
        }
        if (!federated()) {
            throw new ConflictException(ErrorCodes.FEDERATED_DISCOVERY_DISABLED, "Federated chronicle discovery is"
                    + " off; set network.federated to true first.");
        }
        Instant now = clock.instant();
        List<String> replaced = new ArrayList<>();
        for (ChronicleSaltEntity s : salts.findByKindAndNetworkIdAndRetiredAtIsNullOrderByCreatedAtDesc(
                ChronicleSaltEntity.NETWORK, networkId)) {
            s.retire(now);
            salts.save(s);
            replaced.add(s.getId());
        }
        Encrypted e = cipher.encrypt(ref.asString().getBytes(StandardCharsets.UTF_8));
        ChronicleSaltEntity row = new ChronicleSaltEntity("net-" + Ulids.next(now), ChronicleSaltEntity.NETWORK,
                networkId, e.ciphertext(), e.nonce(), now, now.plus(NetworkVocabulary.NETWORK_SALT_ROTATION));
        salts.save(row);
        return new Installed(status(row), replaced);
    }

    /**
     * An installed network salt.
     *
     * @param status the {@code NetworkSaltStatus}
     * @param replaced the ids of the salts it replaced
     */
    public record Installed(Map<String, Object> status, List<String> replaced) {}

    /** The public {@code NetworkSaltStatus} of a salt (never its value). */
    static Map<String, Object> status(ChronicleSaltEntity s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("networkId", s.getNetworkId());
        out.put("saltId", s.getId());
        out.put("createdAt", s.getCreatedAt().toString());
        out.put("rotatesAt", s.getRotatesAt().toString());
        return out;
    }

    private Salt decrypt(ChronicleSaltEntity s) {
        byte[] value = cipher.decrypt(new Encrypted(s.getCiphertext(), s.getNonce()));
        return new Salt(s.getId(), s.getKind(), s.getNetworkId(), value, s.getCreatedAt(), s.getRotatesAt());
    }
}

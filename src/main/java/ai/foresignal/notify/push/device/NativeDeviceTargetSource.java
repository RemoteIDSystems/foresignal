package ai.foresignal.notify.push.device;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.PushFilters;
import ai.foresignal.notify.push.PushResult;
import ai.foresignal.notify.push.PushTarget;
import ai.foresignal.notify.push.PushTargetSource;
import ai.foresignal.notify.push.provider.PushProviderEntity;
import ai.foresignal.notify.push.provider.PushProviderRepository;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The native push targets (prompt 032): {@code active} rows of {@code push_device} whose filters
 * accept the notification and whose app an enabled provider serves (a device of a deleted or
 * disabled provider receives nothing until another provider serves it).
 *
 * <p>Outcomes: a send updates {@code last_success_at} and clears {@code failure_count}; a gone
 * device (APNs 410, {@code BadDeviceToken}, {@code DeviceTokenNotForTopic}; FCM
 * {@code UNREGISTERED}, {@code SENDER_ID_MISMATCH}, an {@code INVALID_ARGUMENT} naming the token)
 * is deleted; other failures count {@code failure_count} and {@code last_failure_at}. Each update
 * is one statement in its own transaction (it runs after the dispatch row's transaction).
 */
@Component
public class NativeDeviceTargetSource implements PushTargetSource {

    private static final Logger log = LoggerFactory.getLogger(NativeDeviceTargetSource.class);

    private final PushDeviceRepository devices;
    private final PushProviderRepository providers;
    private final CredentialCipher cipher;
    private final NamedParameterJdbcTemplate jdbc;
    private volatile ai.foresignal.notify.push.PushRestrictionGate gate;

    /** The read-restriction gate (prompt 035); optional so slices without it still fan out. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setRestrictionGate(ai.foresignal.notify.push.PushRestrictionGate gate) {
        this.gate = gate;
    }

    /** Creates the source. */
    public NativeDeviceTargetSource(PushDeviceRepository devices, PushProviderRepository providers,
            CredentialCipher cipher, NamedParameterJdbcTemplate jdbc) {
        this.devices = devices;
        this.providers = providers;
        this.cipher = cipher;
        this.jdbc = jdbc;
    }

    @Override
    public String targetType() {
        return NativeDeviceTarget.TYPE;
    }

    @Override
    public List<PushTarget> targets(NotificationMessage message) {
        List<PushProviderEntity> enabled = providers.findAllByOrderByIdAsc().stream()
                .filter(p -> p.isEnabled() && p.hasSecret()).toList();
        List<PushTarget> out = new ArrayList<>();
        for (PushDeviceEntity row : devices.findByStatusOrderByCreatedAtAsc(PushDeviceService.ACTIVE)) {
            if (!served(row, enabled) || !PushFilters.fromMap(row.getFilters()).matches(message)
                    || (gate != null && !gate.allows(row.getTokenId(), message))) {
                continue;
            }
            target(row).ifPresent(out::add);
        }
        return out;
    }

    private static boolean served(PushDeviceEntity row, List<PushProviderEntity> enabled) {
        for (PushProviderEntity p : enabled) {
            if (!p.getType().equals(row.getTransport()) || !p.getAppIds().contains(row.getAppId())) {
                continue;
            }
            if (!NativeDeviceTarget.APNS.equals(row.getTransport())) {
                return true;
            }
            String keyEnvironment = String.valueOf(p.getConfig().get("keyEnvironment"));
            if ("both".equals(keyEnvironment) || keyEnvironment.equals(row.getEnvironment())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<PushTarget> find(String targetId) {
        UUID id;
        try {
            id = UUID.fromString(targetId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return devices.findById(id).filter(row -> PushDeviceService.ACTIVE.equals(row.getStatus()))
                .flatMap(this::target);
    }

    /** The target of a row, with its token decrypted; empty when the token cannot be decrypted. */
    public Optional<PushTarget> target(PushDeviceEntity row) {
        try {
            String token = cipher.decryptToString(new Encrypted(row.getTokenCiphertext(), row.getTokenNonce()));
            return Optional.of(new NativeDeviceTarget(row.getId(), row.getTransport(), row.getPlatform(), row.getAppId(),
                    row.getEnvironment(), token, row.getContentLevel(), row.getLang()));
        } catch (RuntimeException e) {
            log.error("The token of push device {} cannot be decrypted: {}", row.getId(), e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    @Override
    public void onOutcome(PushTarget target, PushResult result, Instant at) {
        if (!(target instanceof NativeDeviceTarget device)) {
            return;
        }
        try {
            MapSqlParameterSource p = new MapSqlParameterSource("id", device.deviceId())
                    .addValue("at", Timestamp.from(at)).addValue("status", result.status());
            switch (result) {
                case PushResult.Sent s -> jdbc.update("""
                        update push_device set last_success_at = :at, last_status = :status, failure_count = 0
                         where id = :id
                        """, p);
                case PushResult.Gone g -> jdbc.update("delete from push_device where id = :id", p);
                case PushResult.Retry r when !r.counted() -> { }
                default -> jdbc.update("""
                        update push_device set failure_count = failure_count + 1, last_failure_at = :at,
                               last_status = :status
                         where id = :id
                        """, p);
            }
        } catch (RuntimeException e) {
            log.warn("Could not record the push outcome of device {}: {}", device.deviceId(), e.getMessage());
        }
    }
}

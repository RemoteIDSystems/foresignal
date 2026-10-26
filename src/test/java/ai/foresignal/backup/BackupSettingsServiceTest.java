package ai.foresignal.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.export.ArchiveSettings;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.SecretKeyStore;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link BackupSettingsService} (prompt R2-017): validation, the replace-on-PUT round trip, and S3 secrets that are
 * write-only and stored only as ciphertext.
 */
class BackupSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String ACCESS = "AKIABACKUPEXAMPLEKEY";
    static final String SECRET = "backup/secret/EXAMPLEKEY+value";

    final AtomicReference<BackupSettings> stored = new AtomicReference<>();
    CredentialCipher cipher;
    BackupSettingsService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp(@TempDir Path dir) {
        cipher = new CredentialCipher(new SecretKeyStore(new ForesignalProperties(dir, dir, false, "test")), JSON);
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.BACKUP), eq(BackupSettings.class), any())).thenAnswer(inv ->
                stored.get() != null ? stored.get() : ((Supplier<BackupSettings>) inv.getArgument(2)).get());
        doAnswer(inv -> {
            stored.set(inv.getArgument(1));
            return null;
        }).when(settings).put(eq(SettingKeys.BACKUP), any());
        service = new BackupSettingsService(settings, cipher, mock(OutboundUrlPolicy.class));
    }

    private BackupSettings update(String body) {
        return service.update(JSON.readTree(body.replace('\'', '"')));
    }

    private String decrypt(String cipherText, String nonce) {
        return cipher.decryptToString(new Encrypted(Base64.getDecoder().decode(cipherText),
                Base64.getDecoder().decode(nonce)));
    }

    @Test
    @DisplayName("the defaults are off, local, keep 7, with the config bundle")
    void defaults() {
        BackupSettings value = service.get();
        assertThat(value.enabled()).isFalse();
        assertThat(value.target()).isEqualTo(BackupSettings.LOCAL);
        assertThat(value.keepBackups()).isEqualTo(BackupSettings.DEFAULT_KEEP);
        assertThat(value.includeConfig()).isTrue();
        assertThat(value.schedule()).isNull();
    }

    @Test
    @DisplayName("a full body round-trips, and the view has secretsSet but never a secret")
    void roundTrip() throws Exception {
        update("{'enabled':true,'schedule':'0 30 2 * * *','target':'s3','s3':{'endpoint':'https://s3.example.invalid',"
                + "'bucket':'fs-backups','prefix':'nightly/','region':'eu-west-1','pathStyle':true},'keepBackups':14,"
                + "'includeConfig':false,'secrets':{'accessKey':'" + ACCESS + "','secretKey':'" + SECRET + "'}}");

        BackupSettings value = service.get();
        assertThat(value.enabled()).isTrue();
        assertThat(value.schedule()).isEqualTo("0 30 2 * * *");
        assertThat(value.target()).isEqualTo(BackupSettings.S3_TARGET);
        assertThat(value.s3().bucket()).isEqualTo("fs-backups");
        assertThat(value.s3().prefix()).isEqualTo("nightly/");
        assertThat(value.s3().region()).isEqualTo("eu-west-1");
        assertThat(value.s3().pathStyle()).isTrue();
        assertThat(value.keepBackups()).isEqualTo(14);
        assertThat(value.includeConfig()).isFalse();

        // Encrypted at rest: the stored record serializes without either secret, and both decrypt back.
        String persisted = JSON.writeValueAsString(value);
        assertThat(persisted).doesNotContain(ACCESS).doesNotContain(SECRET);
        assertThat(decrypt(value.accessKeyCipher(), value.accessKeyNonce())).isEqualTo(ACCESS);
        assertThat(decrypt(value.secretKeyCipher(), value.secretKeyNonce())).isEqualTo(SECRET);

        Map<String, Object> view = service.view(value, null);
        assertThat(view).doesNotContainKey("secrets").containsEntry("keepBackups", 14);
        assertThat(view.get("secretsSet")).asInstanceOf(InstanceOfAssertFactories.LIST)
                .containsExactly("accessKey", "secretKey");
        assertThat(JSON.writeValueAsString(view)).doesNotContain(ACCESS).doesNotContain(SECRET);
    }

    @Test
    @DisplayName("PUT replaces: omitted members take their defaults, omitted secrets are kept, empty ones cleared")
    void replaces() {
        update("{'enabled':true,'target':'s3','s3':{'endpoint':'https://s3.example.invalid','bucket':'b'},"
                + "'keepBackups':3,'schedule':'0 0 1 * * *','secrets':{'accessKey':'" + ACCESS + "','secretKey':'"
                + SECRET + "'}}");
        update("{'enabled':false,'target':'s3','s3':{'endpoint':'https://s3.example.invalid','bucket':'b'}}");

        BackupSettings value = service.get();
        assertThat(value.schedule()).isNull();
        assertThat(value.keepBackups()).isEqualTo(BackupSettings.DEFAULT_KEEP);
        assertThat(value.hasSecrets()).isTrue();

        update("{'enabled':false,'target':'local','keepBackups':null,'secrets':{'secretKey':''}}");
        value = service.get();
        assertThat(value.keepBackups()).isNull();
        assertThat(value.accessKeyCipher()).isNotNull();
        assertThat(value.secretKeyCipher()).isNull();
        assertThat(value.hasSecrets()).isFalse();
    }

    @Test
    @DisplayName("every bad member is reported in one 400 and nothing is stored")
    void validation() {
        assertThatThrownBy(() -> update("{'target':'tape','schedule':'nightly','keepBackups':0,'includeConfig':'yes',"
                + "'secrets':{'token':'x'},'flavor':'red'}"))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                        .extracting(FieldError::field)
                        .containsExactlyInAnyOrder("enabled", "target", "schedule", "keepBackups", "includeConfig",
                                "secrets.token", "flavor"));
        assertThatThrownBy(() -> update("{'enabled':true,'target':'external'}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).containsExactly("root"));
        assertThatThrownBy(() -> update("{'enabled':true,'target':'external','root':'relative/dir'}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).containsExactly("root"));
        assertThatThrownBy(() -> update("{'enabled':true,'target':'s3'}"))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                        .extracting(FieldError::field).containsExactlyInAnyOrder("s3.endpoint", "s3.bucket"));
        assertThatThrownBy(() -> update("{'enabled':true,'target':'local','keepBackups':10001}"))
                .isInstanceOf(ValidationException.class);
        assertThat(stored.get()).isNull();
    }

    @Test
    @DisplayName("local and external map to the external-disk tier, s3 to the S3 tier with the same secrets")
    void tierSettings() {
        update("{'enabled':true,'target':'external','root':'/mnt/backup'}");
        ArchiveSettings disk = service.get().tierSettings("/mnt/backup");
        assertThat(disk.target()).isEqualTo(ArchiveSettings.LOCAL);
        assertThat(disk.root()).isEqualTo("/mnt/backup");

        update("{'enabled':true,'target':'s3','s3':{'endpoint':'https://s3.example.invalid','bucket':'b'},"
                + "'secrets':{'accessKey':'" + ACCESS + "','secretKey':'" + SECRET + "'}}");
        BackupSettings value = service.get();
        ArchiveSettings s3 = value.tierSettings(null);
        assertThat(s3.target()).isEqualTo(ArchiveSettings.S3_TARGET);
        assertThat(s3.s3().bucket()).isEqualTo("b");
        assertThat(s3.secretKeyCipher()).isEqualTo(value.secretKeyCipher());
        assertThat(s3.root()).isEmpty();
    }
}

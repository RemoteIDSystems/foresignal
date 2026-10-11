package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

class SecretKeyStoreTest {

    @Test
    @DisplayName("creates a 32-byte key when none exists")
    void createsKey(@TempDir Path dataDir) throws IOException {
        var store = new SecretKeyStore(dataDir);

        Path keyFile = dataDir.resolve(SecretKeyStore.KEY_FILE_NAME);
        assertThat(keyFile).exists();
        assertThat(Files.readAllBytes(keyFile)).hasSize(SecretKeyStore.KEY_LENGTH_BYTES);
        assertThat(store.key().getAlgorithm()).isEqualTo("AES");
        assertThat(store.key().getEncoded()).hasSize(SecretKeyStore.KEY_LENGTH_BYTES);
    }

    @Test
    @DisplayName("creates the data directory if it is missing")
    void createsDataDirectory(@TempDir Path parent) {
        Path dataDir = parent.resolve("nested").resolve("data");

        new SecretKeyStore(dataDir);

        assertThat(dataDir).isDirectory();
        assertThat(dataDir.resolve(SecretKeyStore.KEY_FILE_NAME)).exists();
    }

    @Test
    @DisplayName("the key file is readable only by its owner")
    @DisabledOnOs(OS.WINDOWS)
    void restrictsPermissions(@TempDir Path dataDir) throws IOException {
        new SecretKeyStore(dataDir);

        Set<java.nio.file.attribute.PosixFilePermission> permissions =
                Files.getPosixFilePermissions(dataDir.resolve(SecretKeyStore.KEY_FILE_NAME));

        assertThat(PosixFilePermissions.toString(permissions)).isEqualTo("rw-------");
    }

    @Test
    @DisplayName("reloads exactly the same key on restart")
    void reloadsSameKey(@TempDir Path dataDir) {
        byte[] first = new SecretKeyStore(dataDir).key().getEncoded();

        byte[] second = new SecretKeyStore(dataDir).key().getEncoded();

        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("two installations get different keys")
    void generatesDistinctKeys(@TempDir Path one, @TempDir Path two) {
        assertThat(new SecretKeyStore(one).key().getEncoded())
                .isNotEqualTo(new SecretKeyStore(two).key().getEncoded());
    }

    @Test
    @DisplayName("refuses a key file of the wrong length rather than using a weak key")
    void rejectsWrongLengthKey(@TempDir Path dataDir) throws IOException {
        Files.write(dataDir.resolve(SecretKeyStore.KEY_FILE_NAME), new byte[16]);

        assertThatThrownBy(() -> new SecretKeyStore(dataDir))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("16 bytes")
                .hasMessageContaining("expected 32");
    }

    @Test
    @DisplayName("refuses an empty key file")
    void rejectsEmptyKeyFile(@TempDir Path dataDir) throws IOException {
        Files.write(dataDir.resolve(SecretKeyStore.KEY_FILE_NAME), new byte[0]);

        assertThatThrownBy(() -> new SecretKeyStore(dataDir))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("the error explains the consequence of deleting the key")
    void rejectionMessageIsActionable(@TempDir Path dataDir) throws IOException {
        Files.write(dataDir.resolve(SecretKeyStore.KEY_FILE_NAME), new byte[7]);

        assertThatThrownBy(() -> new SecretKeyStore(dataDir))
                .hasMessageContaining("backup")
                .hasMessageContaining("re-enter every upstream credential");
    }

    @Test
    void exposesTheKeyFileLocation(@TempDir Path dataDir) {
        assertThat(new SecretKeyStore(dataDir).keyFile())
                .isEqualTo(dataDir.resolve(SecretKeyStore.KEY_FILE_NAME));
    }
}

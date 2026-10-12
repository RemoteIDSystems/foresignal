package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.PartitionManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Each {@code /v1/system} warning condition (prompt 019). */
class SystemWarningsTest {

    final AppSettingService settings = mock(AppSettingService.class);
    final PartitionManager partitions = mock(PartitionManager.class);
    final RetentionJob job = mock(RetentionJob.class);

    SystemWarnings warnings(String bind, boolean openReads, String publicBaseUrl) {
        when(settings.getOrDefault(eq(SettingKeys.SECURITY), eq(SecuritySettings.class), any()))
                .thenReturn(new SecuritySettings(openReads));
        when(settings.getOrDefault(eq(SettingKeys.SERVER), eq(ServerSettings.class), any()))
                .thenReturn(new ServerSettings(publicBaseUrl));
        for (HistoryTier tier : HistoryTier.values()) {
            when(partitions.headroom(tier)).thenReturn(300.0);
            when(partitions.block(tier)).thenReturn(100L);
        }
        when(job.lastLags()).thenReturn(List.of());
        return new SystemWarnings(settings, partitions, job, bind);
    }

    static HistoryStats.Snapshot snapshot(double rate) {
        return new HistoryStats.Snapshot(List.of(), rate, 0, Instant.EPOCH);
    }

    List<String> codes(SystemWarnings w, double rate) {
        return w.warnings(snapshot(rate)).stream().map(SystemWarnings.Warning::code).toList();
    }

    @Test
    @DisplayName("a quiet local installation has no warnings")
    void none() {
        assertThat(codes(warnings("127.0.0.1", true, null), 10)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "192.168.1.20", "10.0.0.5", "172.20.1.1", "100.100.1.1", "169.254.1.1",
            "fd12::1", "::1", "foresignal.local", "localhost"})
    @DisplayName("local binds and hosts do not count as exposed")
    void localIsNotExposed(String host) {
        assertThat(SystemWarnings.isLocal(host)).isTrue();
        assertThat(codes(warnings(host, true, "http://" + (host.contains(":") ? "[" + host + "]" : host) + ":8080"), 0))
                .doesNotContain("OPEN_READS_EXPOSED");
    }

    @Test
    @DisplayName("OPEN_READS_EXPOSED: a public base URL host or a public bind with open reads")
    void exposed() {
        assertThat(codes(warnings("127.0.0.1", true, "https://foresignal.example.com"), 0)).contains("OPEN_READS_EXPOSED");
        assertThat(codes(warnings("203.0.113.7", true, null), 0)).contains("OPEN_READS_EXPOSED");
        assertThat(codes(warnings("203.0.113.7", false, "https://foresignal.example.com"), 0)).isEmpty();
    }

    @Test
    @DisplayName("OPEN_READS_ALL_INTERFACES (info): bind 0.0.0.0 with open reads")
    void allInterfaces() {
        List<SystemWarnings.Warning> list = warnings("0.0.0.0", true, null).warnings(snapshot(0));
        assertThat(list).extracting(SystemWarnings.Warning::code).containsExactly("OPEN_READS_ALL_INTERFACES");
        assertThat(list.getFirst().severity()).isEqualTo("info");
        assertThat(codes(warnings("0.0.0.0", false, null), 0)).isEmpty();
    }

    @Test
    @DisplayName("HISTORY_RATE_HIGH above 20,000 versions per minute")
    void historyRate() {
        assertThat(codes(warnings("127.0.0.1", false, null), 20_001)).containsExactly("HISTORY_RATE_HIGH");
        assertThat(codes(warnings("127.0.0.1", false, null), 20_000)).isEmpty();
    }

    @Test
    @DisplayName("PARTITION_HEADROOM_LOW when a tier has less than one block ahead")
    void headroom() {
        SystemWarnings w = warnings("127.0.0.1", false, null);
        when(partitions.headroom(HistoryTier.TRACK)).thenReturn(50.0);
        assertThat(codes(w, 0)).containsExactly("PARTITION_HEADROOM_LOW");
    }

    @Test
    @DisplayName("RETENTION_CONSUMER_LAG after a run released a clamp")
    void consumerLag() {
        SystemWarnings w = warnings("127.0.0.1", false, null);
        when(job.lastLags()).thenReturn(List.of(new RetentionRun.Lag("trigger.cursor", 5, Duration.ofDays(7))));
        assertThat(codes(w, 0)).containsExactly("RETENTION_CONSUMER_LAG");
    }

    @Test
    @DisplayName("host names other than localhost and .local are never resolved and count as not local")
    void hostNames() {
        assertThat(SystemWarnings.isLocal("foresignal.example.com")).isFalse();
        assertThat(SystemWarnings.isLocal("8.8.8.8")).isFalse();
        assertThat(SystemWarnings.host("http://[fd00::1]:8080/x")).isEqualTo("fd00::1");
        assertThat(SystemWarnings.isAllInterfaces("::")).isTrue();
    }
}

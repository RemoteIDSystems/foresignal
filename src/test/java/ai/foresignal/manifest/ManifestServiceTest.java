package ai.foresignal.manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Analysis manifests (prompt 116): the content hash is order-independent and deduplicates identical manifests,
 * registry hashes are recorded, and large input sets keep only the query, the count and the hash of the ordered
 * refs.
 */
class ManifestServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    private static ManifestService service(JdbcTemplate jdbc) {
        return new ManifestService(jdbc, JSON, new RegistryHashes(), new BuildInfo("1.0.0-test", "abc123"),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static ManifestRequest request(Map<String, Object> config) {
        return ManifestRequest.of("poisson-tail", "1.0.0").config(config).registries("originators.json")
                .asOf(42L).inputs(List.of("activity-sample:a/b/c"));
    }

    @Test
    @DisplayName("the content hash ignores map order, and differs for a different configuration")
    void contentHash() {
        ManifestService service = service(mock(JdbcTemplate.class));
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("alpha", 0.001);
        a.put("bucket", "PT1H");
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("bucket", "PT1H");
        b.put("alpha", 0.001);
        String ha = CanonicalJson.hash(service.content(request(a)), JSON);
        String hb = CanonicalJson.hash(service.content(request(b)), JSON);
        assertThat(ha).isEqualTo(hb).hasSize(64);
        Map<String, Object> c = new LinkedHashMap<>(a);
        c.put("alpha", 0.01);
        assertThat(CanonicalJson.hash(service.content(request(c)), JSON)).isNotEqualTo(ha);
    }

    @Test
    @DisplayName("an identical manifest returns the stored id and is not inserted again")
    void deduplicates() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq(String.class), anyString())).thenReturn(List.of("01JEXISTING00000000000000"));
        String id = service(jdbc).record(request(Map.of("alpha", 0.001)));
        assertThat(id).isEqualTo("01JEXISTING00000000000000");
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("a new manifest is inserted once, keyed by its content hash")
    void inserts() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq(String.class), anyString())).thenReturn(List.of());
        when(jdbc.queryForObject(anyString(), eq(String.class), anyString())).thenReturn("01JNEW0000000000000000000");
        assertThat(service(jdbc).record(request(Map.of("alpha", 0.001)))).isEqualTo("01JNEW0000000000000000000");
    }

    @Test
    @DisplayName("registry hashes and the build are recorded; a missing registry says so")
    void registriesAndBuild() {
        ManifestService service = service(mock(JdbcTemplate.class));
        Map<String, Object> content = service.content(ManifestRequest.of("x", "1").registries("originators.json",
                "canonical-rules.json", "no-such-registry.json"));
        @SuppressWarnings("unchecked")
        Map<String, Object> registries = (Map<String, Object>) content.get("registries");
        assertThat((String) registries.get("originators.json")).hasSize(64);
        assertThat((String) registries.get("canonical-rules.json")).hasSize(64);
        // signal-families.json exists since prompt 119, so the missing case names a registry that does not.
        assertThat(registries.get("no-such-registry.json")).isEqualTo(RegistryHashes.MISSING);
        assertThat(content.get("build")).isEqualTo(Map.of("version", "1.0.0-test", "gitCommit", "abc123"));
        assertThat(content.get("asOfSeq")).isNull();
        assertThat(content.get("model")).isNull();
    }

    @Test
    @DisplayName("up to 10,000 refs are explicit; above, only the query, the count and the ordered hash")
    void largeInputs() {
        ManifestService service = service(mock(JdbcTemplate.class));
        List<String> few = List.of("record:a/1@1", "record:a/2@2");
        Map<String, Object> small = service.inputs(few, null);
        assertThat(small.get("refs")).isEqualTo(few);
        assertThat(small.get("count")).isEqualTo(2L);
        List<String> many = new ArrayList<>();
        for (int i = 0; i < ManifestService.MAX_EXPLICIT_REFS + 1; i++) {
            many.add("record:a/" + i + "@" + i);
        }
        Map<String, Object> large = service.inputs(many, Map.of("table", "record_version", "fromSeq", 1));
        assertThat(large.get("refs")).isNull();
        assertThat(large.get("count")).isEqualTo((long) many.size());
        assertThat(large.get("query")).isEqualTo(Map.of("fromSeq", 1, "table", "record_version"));
        assertThat(large.get("hash")).isEqualTo(CanonicalJson.sha256(String.join("\n", many)));
        List<String> reordered = new ArrayList<>(many);
        java.util.Collections.swap(reordered, 0, 1);
        assertThat(service.inputs(reordered, Map.of()).get("hash")).isNotEqualTo(large.get("hash"));
        Map<String, Object> queryOnly = service.inputs(null, Map.of("fromSeq", 1));
        assertThat(queryOnly.get("count")).isEqualTo(0L);
        assertThat((String) queryOnly.get("hash")).hasSize(64);
    }

    @Test
    @DisplayName("an unknown manifest is 404 UNKNOWN_MANIFEST")
    void unknown() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        assertThatThrownBy(() -> service(jdbc).get("01JNOPE000000000000000000")).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("01JNOPE");
    }

    @Test
    @DisplayName("the build commit comes from git.properties, then build-info, then the environment")
    void buildCommit() {
        assertThat(BuildInfo.commit(null, null, null)).isEqualTo(BuildInfo.UNKNOWN);
        assertThat(BuildInfo.commit(null, null, " deadbeef ")).isEqualTo("deadbeef");
    }
}

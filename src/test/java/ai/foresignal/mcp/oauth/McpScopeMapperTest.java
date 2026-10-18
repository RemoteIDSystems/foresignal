package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link McpScopeMapper} (prompt 141): claim shapes, the mapping, admin never mappable, and the reverse lookups. */
class McpScopeMapperTest {

    static final Map<String, String> DEFAULT = McpAuthSettings.DEFAULT_SCOPE_MAP;

    @Test
    @DisplayName("scope strings and scp arrays are both read; blanks and duplicates dropped")
    void scopes() {
        assertThat(McpScopeMapper.scopes(" openid  foresignal:read foresignal:read ")).containsExactly("openid",
                "foresignal:read");
        assertThat(McpScopeMapper.scopes(List.of("a", "b c", 7))).containsExactly("a", "b", "c");
        assertThat(McpScopeMapper.scopes(null)).isEmpty();
        assertThat(McpScopeMapper.scopes(42)).isEmpty();
        assertThat(McpScopeMapper.scopes("")).isEmpty();
    }

    @Test
    @DisplayName("the most privileged mapped scope wins; unmapped scopes are ignored")
    void map() {
        assertThat(McpScopeMapper.map(Set.of("foresignal:read"), DEFAULT)).contains(Scope.READ);
        assertThat(McpScopeMapper.map(Set.of("foresignal:read", "foresignal:agent"), DEFAULT)).contains(Scope.AGENT);
        assertThat(McpScopeMapper.map(Set.of("foresignal:agent", "foresignal:read"), DEFAULT)).contains(Scope.AGENT);
        assertThat(McpScopeMapper.map(Set.of("openid", "profile"), DEFAULT)).isEmpty();
        assertThat(McpScopeMapper.map(Set.of(), DEFAULT)).isEmpty();
    }

    @Test
    @DisplayName("admin is never mappable, even when a stored map names it")
    void adminNeverMappable() {
        Map<String, String> map = new LinkedHashMap<>(DEFAULT);
        map.put("foresignal:admin", "admin");
        map.put("weird", "superuser");
        assertThat(McpScopeMapper.map(Set.of("foresignal:admin"), map)).isEmpty();
        assertThat(McpScopeMapper.map(Set.of("foresignal:admin", "foresignal:read"), map)).contains(Scope.READ);
        assertThat(McpScopeMapper.map(Set.of("weird"), map)).isEmpty();
        assertThat(McpScopeMapper.mappable("ADMIN")).isEmpty();
        assertThat(McpScopeMapper.mappable("Agent")).contains(Scope.AGENT);
        assertThat(McpScopeMapper.mappable(null)).isEmpty();
        assertThat(McpScopeMapper.supported(map)).containsExactly("foresignal:agent", "foresignal:read");
        assertThat(McpScopeMapper.scopesFor(Scope.ADMIN, map)).containsExactly("foresignal:admin");
    }

    @Test
    @DisplayName("scopesFor names the exact scopes, else wider ones, else foresignal:<scope>")
    void scopesFor() {
        assertThat(McpScopeMapper.scopesFor(Scope.READ, DEFAULT)).containsExactly("foresignal:read");
        assertThat(McpScopeMapper.scopesFor(Scope.AGENT, DEFAULT)).containsExactly("foresignal:agent");
        assertThat(McpScopeMapper.scopesFor(Scope.READ, Map.of("mcp", "agent"))).containsExactly("mcp");
        assertThat(McpScopeMapper.scopesFor(Scope.READ, Map.of("b", "read", "a", "read"))).containsExactly("a", "b");
        assertThat(McpScopeMapper.scopesFor(Scope.AGENT, Map.of("r", "read"))).containsExactly("foresignal:agent");
        assertThat(McpScopeMapper.scopesFor(Scope.READ, Map.of())).containsExactly("foresignal:read");
    }
}

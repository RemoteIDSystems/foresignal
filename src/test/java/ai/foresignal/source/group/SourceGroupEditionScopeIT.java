package ai.foresignal.source.group;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.briefing.BriefingScope;
import ai.foresignal.briefing.ScopeResolver;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A source group used as an edition scope (prompt SG-002): {@code BriefingScope.sourceGroupIds} resolves to the union
 * of the groups' member sources, and an unknown or deleted group makes the scope match nothing (fail closed) rather
 * than widening to every source.
 */
@IntegrationTest
class SourceGroupEditionScopeIT {

    @Autowired ScopeResolver resolver;
    @Autowired SourceGroupRepository groups;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    private SourceGroupEntity group(List<String> sourceIds) {
        UUID owner = TestUsers.active(principals, users, userRoles, hasher,
                "scope-" + UUID.randomUUID() + "@example.com").getId();
        SourceGroupEntity group = new SourceGroupEntity("sg-" + UUID.randomUUID(), owner);
        group.setName("Group");
        group.setSourceIds(sourceIds);
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        return groups.saveAndFlush(group);
    }

    @Test
    @DisplayName("sourceGroupIds resolve to the union of the groups' member sources")
    void unionOfMembers() {
        SourceGroupEntity a = group(List.of("src-a", "src-b"));
        SourceGroupEntity b = group(List.of("src-b", "src-c"));
        BriefingScope scope = BriefingScope.fromMap(Map.of("sourceGroupIds", List.of(a.getId(), b.getId())));

        ScopeResolver.Resolved resolved = resolver.resolve(scope);
        assertThat(resolved.sources()).containsExactlyInAnyOrder("src-a", "src-b", "src-c");
        assertThat(resolved.filter()).as("a resolvable group does not fail closed").isNull();
    }

    @Test
    @DisplayName("an unknown or empty group makes the scope match nothing, never every source")
    void failsClosed() {
        // An unknown group id: the scope must not widen to all sources.
        ScopeResolver.Resolved unknown = resolver.resolve(
                BriefingScope.fromMap(Map.of("sourceGroupIds", List.of("sg-does-not-exist"))));
        assertThat(unknown.sources()).isEmpty();
        assertThat(unknown.filter()).as("a dangling group fails closed").isNotNull();

        // An existing but empty group also fails closed.
        SourceGroupEntity empty = group(List.of());
        ScopeResolver.Resolved resolved = resolver.resolve(
                BriefingScope.fromMap(Map.of("sourceGroupIds", List.of(empty.getId()))));
        assertThat(resolved.filter()).as("an empty group fails closed").isNotNull();
    }
}

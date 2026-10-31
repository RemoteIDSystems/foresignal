package ai.foresignal.source.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSpecs;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A source group used as a trigger scope (prompt SG-002): {@code sourceGroupId} must name a group the rule's owner
 * owns ({@code 400} otherwise) and expands to the group's member sources in the compiled spec; a group that is empty
 * or deleted fails the rule closed (it matches nothing, never every source); membership edits take effect without
 * rewriting the rule.
 */
@IntegrationTest
class TriggerSourceGroupScopeIT {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TriggerRuleService rules;
    @Autowired RuleSpecs ruleSpecs;
    @Autowired SourceGroupRepository groups;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    private UserEntity user(String email) {
        return TestUsers.active(principals, users, userRoles, hasher, email);
    }

    private SourceGroupEntity group(UUID owner, List<String> sourceIds) {
        SourceGroupEntity group = new SourceGroupEntity("sg-" + UUID.randomUUID(), owner);
        group.setName("Group");
        group.setSourceIds(sourceIds);
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        return groups.saveAndFlush(group);
    }

    private JsonNode rule(String groupId) {
        return JSON.readTree("{\"name\":\"Grouped\",\"sourceGroupId\":\"" + groupId + "\"}");
    }

    @Test
    @DisplayName("sourceGroupId expands to the group's member sources in the compiled spec")
    void expandsToMembers() {
        UserEntity owner = user("trig-owner@example.com");
        SourceGroupEntity group = group(owner.getId(), List.of("src-a", "src-b"));

        TriggerRuleEntity rule = rules.create(rule(group.getId()), owner.getPrincipalId()).rule();
        assertThat(rule.getSourceGroupId()).isEqualTo(group.getId());
        RuleSpec spec = ruleSpecs.compile(rule);
        assertThat(spec.sources()).containsExactlyInAnyOrder("src-a", "src-b");

        // Editing the group's membership takes effect on the next compile, without rewriting the rule.
        group.setSourceIds(List.of("src-a", "src-c"));
        groups.saveAndFlush(group);
        assertThat(ruleSpecs.compile(rules.load(rule.getId())).sources())
                .containsExactlyInAnyOrder("src-a", "src-c");
    }

    @Test
    @DisplayName("a group not owned by the rule owner is 400")
    void foreignGroupRejected() {
        UserEntity owner = user("trig-mine@example.com");
        UserEntity other = user("trig-other@example.com");
        SourceGroupEntity group = group(owner.getId(), List.of("src-a"));

        assertThatThrownBy(() -> rules.create(rule(group.getId()), other.getPrincipalId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("an empty group matches nothing (the spec's source set is the fail-closed sentinel)")
    void emptyGroupFailsClosed() {
        UserEntity owner = user("trig-empty@example.com");
        SourceGroupEntity group = group(owner.getId(), List.of());

        TriggerRuleEntity rule = rules.create(rule(group.getId()), owner.getPrincipalId()).rule();
        assertThat(ruleSpecs.compile(rule).sources()).containsExactly(RuleSpecs.NO_SOURCE_SENTINEL);
    }
}

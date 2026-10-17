package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.object.AnnotationEntity;
import ai.foresignal.object.AnnotationRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Tlp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link AccessResolver} (prompt 130, COL-1): owner, editor, commenter, viewer, group and instance grants, expiry,
 * private visibility, invisible-not-forbidden, admin override, workspace member roles and notes on hidden targets.
 */
class AccessResolverTest {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");
    static final String INV = "investigation:01JA8Q3V7S2K9W4T6Y1B5N0M3C";
    static final String OWNER = "01JA8Q3V7S2K9W4T6Y1B5N0M00";
    static final String ALICE = "01JA8Q3V7S2K9W4T6Y1B5N0M01";
    static final String BOB = "01JA8Q3V7S2K9W4T6Y1B5N0M02";
    static final String CAROL = "01JA8Q3V7S2K9W4T6Y1B5N0M03";
    static final String GROUP = "01JA8Q3V7S2K9W4T6Y1B5N0G01";

    ShareableObjectRepository registry;
    ObjectAclRepository acls;
    GroupMemberRepository members;
    AnnotationRepository annotations;
    Map<String, ShareableObjectEntity> rows;
    List<ObjectAclEntity> grants;
    AccessResolver resolver;

    @BeforeEach
    void setUp() {
        registry = mock(ShareableObjectRepository.class);
        acls = mock(ObjectAclRepository.class);
        members = mock(GroupMemberRepository.class);
        annotations = mock(AnnotationRepository.class);
        rows = new HashMap<>();
        grants = new ArrayList<>();
        when(registry.findById(anyString())).thenAnswer(i -> Optional.ofNullable(rows.get(i.<String>getArgument(0))));
        when(registry.findByObjectRefIn(any())).thenAnswer(i -> {
            List<ShareableObjectEntity> out = new ArrayList<>();
            for (Object r : (Iterable<?>) i.getArgument(0)) {
                if (rows.containsKey(r)) {
                    out.add(rows.get(r));
                }
            }
            return out;
        });
        when(acls.findByObjectRefOrderByGrantedAtAscIdAsc(anyString())).thenAnswer(i -> grants.stream()
                .filter(g -> g.getObjectRef().equals(i.getArgument(0))).toList());
        when(acls.findByObjectRefIn(any())).thenAnswer(i -> new ArrayList<>(grants));
        when(members.findByPrincipalId(anyString())).thenReturn(List.of());
        when(members.findByPrincipalId(CAROL)).thenReturn(List.of(new GroupMemberEntity("m1", GROUP, CAROL,
                GroupMemberEntity.MEMBER, OWNER, NOW)));
        ShareableSource workspaces = new ShareableSource() {
            @Override
            public String type() {
                return "workspace";
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return Optional.of(new Defaults(id, OWNER, Visibility.PRIVATE, null));
            }

            @Override
            public AclRole intrinsicRole(String id, String principalId) {
                return BOB.equals(principalId) ? AclRole.EDITOR : null;
            }
        };
        resolver = new AccessResolver(registry, acls, members, annotations, List.of(workspaces),
                new CollabProperties("AMBER", false, 50, false), mock(JdbcTemplate.class),
                Clock.fixed(NOW, ZoneOffset.UTC));
        rows.put(INV, new ShareableObjectEntity(INV, "investigation", OWNER, Visibility.SHARED, "AMBER", NOW));
    }

    static ObjectCaller caller(String principal, Scope scope) {
        return new ObjectCaller(principal, "token:" + principal, scope, false, Set.of());
    }

    void grant(String ref, String grantee, AclRole role, Instant expires) {
        ObjectAclEntity g = new ObjectAclEntity("g" + grants.size(), ref, grantee, role, OWNER, NOW);
        g.setExpiresAt(expires);
        grants.add(g);
    }

    @Test
    @DisplayName("the owner may do everything; a principal without a grant sees nothing")
    void ownerAndStranger() {
        assertThat(resolver.role(caller(OWNER, Scope.READ), INV)).isEqualTo(AclRole.OWNER);
        assertThat(resolver.can(caller(OWNER, Scope.READ), INV, AccessAction.TRANSFER)).isTrue();
        assertThat(resolver.role(caller(ALICE, Scope.AGENT), INV)).isNull();
        assertThat(resolver.can(caller(ALICE, Scope.AGENT), INV, AccessAction.VIEW)).isFalse();
    }

    @Test
    @DisplayName("editor, commenter and viewer grants allow exactly their actions")
    void roles() {
        grant(INV, "principal:" + ALICE, AclRole.EDITOR, null);
        grant(INV, "principal:" + BOB, AclRole.COMMENTER, null);
        ObjectCaller alice = caller(ALICE, Scope.READ);
        ObjectCaller bob = caller(BOB, Scope.READ);
        assertThat(resolver.can(alice, INV, AccessAction.EDIT)).isTrue();
        assertThat(resolver.can(alice, INV, AccessAction.SHARE)).isTrue();
        assertThat(resolver.can(alice, INV, AccessAction.DELETE)).isFalse();
        assertThat(resolver.can(bob, INV, AccessAction.COMMENT)).isTrue();
        assertThat(resolver.can(bob, INV, AccessAction.EDIT)).isFalse();
        grants.clear();
        grant(INV, "principal:" + BOB, AclRole.VIEWER, null);
        assertThat(resolver.can(bob, INV, AccessAction.VIEW)).isTrue();
        assertThat(resolver.can(bob, INV, AccessAction.COMMENT)).isFalse();
    }

    @Test
    @DisplayName("group and instance grants reach their members; instance visibility makes everyone a viewer")
    void groupAndInstance() {
        grant(INV, "group:" + GROUP, AclRole.COMMENTER, null);
        assertThat(resolver.role(caller(CAROL, Scope.READ), INV)).isEqualTo(AclRole.COMMENTER);
        assertThat(resolver.role(caller(ALICE, Scope.READ), INV)).isNull();
        grant(INV, "instance", AclRole.VIEWER, null);
        assertThat(resolver.role(caller(ALICE, Scope.READ), INV)).isEqualTo(AclRole.VIEWER);
        grants.clear();
        rows.get(INV).setVisibility(Visibility.INSTANCE);
        assertThat(resolver.role(caller(ALICE, Scope.READ), INV)).isEqualTo(AclRole.VIEWER);
        assertThat(resolver.role(null, INV)).isNull();
        assertThat(resolver.role(new ObjectCaller(null, "anonymous", null, false, Set.of()), INV)).isNull();
    }

    @Test
    @DisplayName("expired grants and grants on a private object do not count")
    void expiryAndPrivate() {
        grant(INV, "principal:" + ALICE, AclRole.EDITOR, NOW.minusSeconds(1));
        assertThat(resolver.role(caller(ALICE, Scope.READ), INV)).isNull();
        grants.clear();
        grant(INV, "principal:" + ALICE, AclRole.EDITOR, NOW.plusSeconds(60));
        assertThat(resolver.role(caller(ALICE, Scope.READ), INV)).isEqualTo(AclRole.EDITOR);
        rows.get(INV).setVisibility(Visibility.PRIVATE);
        assertThat(resolver.role(caller(ALICE, Scope.READ), INV)).isNull();
    }

    @Test
    @DisplayName("an object the caller cannot see is not found, never forbidden; a weak role is forbidden")
    void invisibleNotForbidden() {
        assertThatThrownBy(() -> resolver.require(caller(ALICE, Scope.READ), INV, AccessAction.VIEW))
                .isInstanceOf(NotFoundException.class).hasFieldOrPropertyWithValue("code", "UNKNOWN_OBJECT");
        grant(INV, "principal:" + ALICE, AclRole.VIEWER, null);
        assertThatThrownBy(() -> resolver.require(caller(ALICE, Scope.READ), INV, AccessAction.EDIT))
                .isInstanceOf(ForesignalException.class).hasFieldOrPropertyWithValue("code", "FORBIDDEN");
        assertThat(resolver.require(caller(ALICE, Scope.READ), INV, AccessAction.VIEW).getObjectRef()).isEqualTo(INV);
    }

    @Test
    @DisplayName("admins see and may do everything, but admin scope is not a grant for ObjectGrants")
    void adminOverride() {
        ObjectCaller admin = caller(ALICE, Scope.ADMIN);
        assertThat(resolver.role(admin, INV)).isEqualTo(AclRole.OWNER);
        assertThat(resolver.can(admin, INV, AccessAction.DELETE)).isTrue();
        assertThat(resolver.grantedRole(INV, admin)).isNull();
        assertThat(resolver.tlp(INV)).contains("AMBER");
        assertThat(resolver.tlpOf(INV)).isEqualTo(Tlp.AMBER);
    }

    @Test
    @DisplayName("a workspace member keeps the role the workspace gives it, even while private")
    void workspaceMembers() {
        String ws = "workspace:01JA8Q3V7S2K9W4T6Y1B5N0W01";
        rows.put(ws, new ShareableObjectEntity(ws, "workspace", OWNER, Visibility.PRIVATE, "AMBER", NOW));
        assertThat(resolver.role(caller(BOB, Scope.READ), ws)).isEqualTo(AclRole.EDITOR);
        assertThat(resolver.grantedRole(ws, caller(BOB, Scope.READ))).isEqualTo("editor");
        assertThat(resolver.role(caller(ALICE, Scope.READ), ws)).isNull();
    }

    @Test
    @DisplayName("a note on a shareable target is invisible to those who cannot see the target")
    void notesFollowTheirTarget() {
        String note = "annotation:01JA8Q3V7S2K9W4T6Y1B5N0A01";
        rows.put(note, new ShareableObjectEntity(note, "annotation", BOB, Visibility.INSTANCE, "AMBER", NOW));
        AnnotationEntity a = new AnnotationEntity("01JA8Q3V7S2K9W4T6Y1B5N0A01", INV, "note", BOB, NOW);
        when(annotations.findById("01JA8Q3V7S2K9W4T6Y1B5N0A01")).thenReturn(Optional.of(a));
        assertThat(resolver.role(caller(BOB, Scope.READ), note)).isEqualTo(AclRole.OWNER);
        assertThat(resolver.role(caller(ALICE, Scope.READ), note)).isNull();
        grant(INV, "principal:" + ALICE, AclRole.VIEWER, null);
        assertThat(resolver.role(caller(ALICE, Scope.READ), note)).isEqualTo(AclRole.VIEWER);
    }

    @Test
    @DisplayName("batch roles leave non-shareable references out and map invisible ones to null")
    void batchRoles() {
        grant(INV, "principal:" + ALICE, AclRole.COMMENTER, null);
        Map<String, AclRole> roles = resolver.roles(caller(ALICE, Scope.READ), List.of(INV,
                "situation:01JA8Q3V7S2K9W4T6Y1B5N0S01", "evidence-item:01JA8Q3V7S2K9W4T6Y1B5N0E01"));
        assertThat(roles).containsEntry(INV, AclRole.COMMENTER).doesNotContainKey(
                "situation:01JA8Q3V7S2K9W4T6Y1B5N0S01");
        assertThat(roles).containsKey("evidence-item:01JA8Q3V7S2K9W4T6Y1B5N0E01");
        assertThat(roles.get("evidence-item:01JA8Q3V7S2K9W4T6Y1B5N0E01")).isNull();
        assertThat(resolver.granteesOf(CAROL)).containsExactlyInAnyOrder("instance", "principal:" + CAROL,
                "group:" + GROUP);
        assertThat(AccessResolver.isShareable("record:src/x")).isFalse();
        assertThat(AccessResolver.isShareable(INV)).isTrue();
    }
}

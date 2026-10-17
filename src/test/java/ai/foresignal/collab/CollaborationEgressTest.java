package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Scope;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * COL-1 at the envelope boundary (prompt 130): {@link CollabEnvelopeDecorator} drops the work products the caller may
 * not see, labels the ones it may, adds the access-list actions for editors, and never touches data objects.
 */
class CollaborationEgressTest {

    static final String INV = "investigation:01JA8Q3V7S2K9W4T6Y1B5N0M3C";
    static final String NOTE = "evidence-item:01JA8Q3V7S2K9W4T6Y1B5N0E01";
    static final String SIT = "situation:01JA8Q3V7S2K9W4T6Y1B5N0S01";

    @SuppressWarnings("unchecked")
    CollabEnvelopeDecorator decorator(Map<String, AclRole> roles, Map<String, List<String>> tags) {
        AccessResolver access = mock(AccessResolver.class);
        when(access.roles(any(), any())).thenReturn(roles);
        Map<String, String> tlps = new HashMap<>();
        roles.keySet().forEach(r -> tlps.put(r, "RED"));
        when(access.tlps(any())).thenReturn(tlps);
        TagService tagService = mock(TagService.class);
        when(tagService.visibleNames(any(), any())).thenReturn(tags);
        ObjectProvider<TagService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(tagService);
        return new CollabEnvelopeDecorator(access, provider);
    }

    static Map<ObjectRef, ObjectEnvelope> envelopes(String... refs) {
        Map<ObjectRef, ObjectEnvelope> out = new LinkedHashMap<>();
        for (String r : refs) {
            ObjectRef ref = ObjectRef.parse(r);
            out.put(ref, ObjectEnvelope.builder(ref).title(r).build());
        }
        return out;
    }

    static ResolveContext context() {
        return new ResolveContext(new ObjectCaller("01JA8Q3V7S2K9W4T6Y1B5N0M01", "token:a", Scope.READ, false,
                Set.of()), null);
    }

    @Test
    @DisplayName("an unshared work product is dropped, a visible one carries its TLP label, data objects pass")
    void invisibleWorkProductsAreDropped() {
        Map<String, AclRole> roles = new HashMap<>();
        roles.put(INV, null);
        roles.put(NOTE, AclRole.VIEWER);
        Map<ObjectRef, ObjectEnvelope> out = decorator(roles, Map.of(SIT, List.of("red-sea")))
                .decorate(envelopes(INV, NOTE, SIT), context());
        assertThat(out.keySet()).extracting(ObjectRef::format).containsExactly(NOTE, SIT);
        assertThat(out.get(ObjectRef.parse(NOTE)).tlp()).isEqualTo("RED");
        assertThat(out.get(ObjectRef.parse(SIT)).tlp()).isNull();
        assertThat(out.get(ObjectRef.parse(SIT)).tags()).containsExactly("red-sea");
        assertThat(out.get(ObjectRef.parse(NOTE)).actions()).extracting("id").doesNotContain("share", "edit-acl");
    }

    @Test
    @DisplayName("editors and owners get the share and edit-acl actions")
    void editorsShare() {
        Map<String, AclRole> roles = new HashMap<>();
        roles.put(INV, AclRole.EDITOR);
        ObjectEnvelope e = decorator(roles, Map.of()).decorate(envelopes(INV), context()).get(ObjectRef.parse(INV));
        assertThat(e.actions()).extracting("id").contains("share", "edit-acl");
        assertThat(e.actions()).filteredOn(a -> a.id().equals("share")).extracting("scope").containsExactly("agent");
    }
}

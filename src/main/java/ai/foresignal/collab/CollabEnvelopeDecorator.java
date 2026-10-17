package ai.foresignal.collab;

import ai.foresignal.object.EnvelopeDecorator;
import ai.foresignal.object.ObjectAction;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ResolveContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The collaboration fields of every envelope (prompt 130): work products the caller may not see are dropped
 * (invisible, not forbidden: COL-1), a work product carries its TLP label, and every object carries the names of the
 * caller's visible tags. Owners and editors also get the {@code share} and {@code edit-acl} actions.
 */
@Component
public class CollabEnvelopeDecorator implements EnvelopeDecorator {

    private final AccessResolver access;
    private final ObjectProvider<TagService> tags;

    /** Creates the decorator (the tag service is looked up lazily: it resolves objects itself). */
    public CollabEnvelopeDecorator(AccessResolver access, ObjectProvider<TagService> tags) {
        this.access = access;
        this.tags = tags;
    }

    @Override
    public Map<ObjectRef, ObjectEnvelope> decorate(Map<ObjectRef, ObjectEnvelope> envelopes, ResolveContext context) {
        Set<String> canonical = new LinkedHashSet<>();
        Set<String> tagKeys = new LinkedHashSet<>();
        for (ObjectEnvelope e : envelopes.values()) {
            canonical.add(e.ref());
            tagKeys.add(e.objectRef().withoutSeq().format());
        }
        Map<String, AclRole> roles = access.roles(context.caller(), canonical);
        Map<String, String> tlps = access.tlps(roles.keySet());
        TagService tagService = tags.getIfAvailable();
        Map<String, List<String>> names = tagService == null ? Map.of()
                : tagService.visibleNames(tagKeys, context.caller());
        Map<ObjectRef, ObjectEnvelope> out = new LinkedHashMap<>();
        for (Map.Entry<ObjectRef, ObjectEnvelope> entry : envelopes.entrySet()) {
            ObjectEnvelope e = entry.getValue();
            String ref = e.ref();
            String tlp = null;
            List<ObjectAction> extra = new ArrayList<>();
            if (roles.containsKey(ref)) {
                AclRole role = roles.get(ref);
                if (role == null) {
                    continue;
                }
                tlp = tlps.get(ref);
                if (role.atLeast(AclRole.EDITOR)) {
                    String path = "/v1/objects/" + ref + "/";
                    extra.add(new ObjectAction("share", "Share", "POST", path + "share", "agent"));
                    extra.add(new ObjectAction("edit-acl", "Edit access", "PUT", path + "acl", "agent"));
                }
            }
            List<String> tagNames = names.getOrDefault(e.objectRef().withoutSeq().format(), List.of());
            out.put(entry.getKey(), e.withCollaboration(tagNames, tlp, extra));
        }
        return out;
    }
}

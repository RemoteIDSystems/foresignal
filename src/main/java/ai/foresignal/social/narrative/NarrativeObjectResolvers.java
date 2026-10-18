package ai.foresignal.social.narrative;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The 129 object types of prompt 136 (contract 1.5): {@code narrative:{id}} and {@code claim:{id}}. Narratives and claims
 * carry no geometry of observation: a claim's place is reported, never observed, so its envelope says so in its
 * summary and carries no geometry. Both are public content ({@code CLEAR}); objects derived from them follow 130.
 */
@Configuration(proxyBeanMethods = false)
public class NarrativeObjectResolvers {

    /** The {@code narrative} resolver. */
    @Bean
    public ObjectResolver narrativeObjectResolver(NarrativeStore store) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.NARRATIVE;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return store.narrative(ref.id()).filter(n -> context.existedAt(n.createdAt()))
                        .map(n -> narrativeEnvelope(ref, n, context));
            }
        };
    }

    /** The {@code claim} resolver. */
    @Bean
    public ObjectResolver claimObjectResolver(NarrativeStore store) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.CLAIM;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return store.claim(ref.id()).filter(c -> context.existedAt(c.createdAt()))
                        .map(c -> claimEnvelope(ref, c));
            }
        };
    }

    /** The envelope of a narrative. */
    static ObjectEnvelope narrativeEnvelope(ObjectRef ref, NarrativeStore.Narrative n, ResolveContext context) {
        List<String> sources = new ArrayList<>();
        for (Object p : n.platforms()) {
            if (p instanceof Map<?, ?> m && m.get("platform") != null) {
                sources.add(m.get("platform").toString());
            }
        }
        String summary = n.postCount() + " posts by " + n.uniqueAuthors() + " authors from " + n.evidenceGroupCount()
                + " independent origins on " + String.join(", ", sources) + "; " + n.state() + ", status "
                + n.status() + (n.coordinationFindingId() != null ? "; coordinated-amplification indicators present"
                : "");
        return ObjectEnvelope.builder(ref)
                .title(n.title())
                .summary(summary)
                .status(n.status())
                .time(n.firstObservedAt(), n.lastActivityAt(), n.lastActivityAt())
                .privateObject(false)
                .provenance(List.of(), n.manifestId(), null)
                .tlp(n.tlp())
                .self("/v1/social/narratives/" + n.id())
                .build();
    }

    /** The envelope of a claim. */
    static ObjectEnvelope claimEnvelope(ObjectRef ref, NarrativeStore.Claim c) {
        String place = c.place() == null || c.place().get("name") == null ? null : c.place().get("name").toString();
        String title = ("denied".equals(c.polarity()) ? "Denied: " : "questioned".equals(c.polarity()) ? "Questioned: "
                : "Reported: ") + c.type().replace('-', ' ') + (place == null ? "" : " in " + place);
        String summary = String.format(Locale.ROOT, "A claim (report, not observation) supported by %d posts from %d"
                + " independent origins; extraction %s; grade %s%d; status %s.", c.supportCount(),
                c.evidenceGroupCount(), c.extraction(), c.sourceReliability(),
                SocialEvidenceGrades.credibility(c.status()), c.status());
        return ObjectEnvelope.builder(ref)
                .title(title)
                .summary(summary)
                .status(c.status())
                .time(c.firstObservedAt(), null, c.firstObservedAt())
                .privateObject(false)
                .provenance(List.of(), c.manifestId(), null)
                .tlp(c.tlp())
                .self("/v1/social/claims/" + c.id())
                .build();
    }
}

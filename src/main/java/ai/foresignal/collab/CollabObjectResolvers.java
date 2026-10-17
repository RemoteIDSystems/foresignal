package ai.foresignal.collab;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Ulids;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The resolvers of the six object types prompt 130 adds (contract 1.1): {@code principal:{id or handle}},
 * {@code group:{id}}, {@code tag:{id}}, {@code attachment:{id}}, {@code evidence-item:{id}} and
 * {@code saved-search:{id}}. Principals and groups resolve for any token. The work products resolve here and are then
 * filtered by access list in {@link CollabEnvelopeDecorator}; a personal tag resolves only for its owner.
 */
@Configuration
public class CollabObjectResolvers {

    /** {@code principal:{id or handle}}: the public summary, for any token. */
    @Bean
    public ObjectResolver principalObjectResolver(PrincipalRepository principals) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.PRINCIPAL;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                if (context.caller() == null || context.caller().anonymous()) {
                    return Optional.empty();
                }
                Optional<PrincipalEntity> p = Ulids.isUlid(ref.id()) ? principals.findById(ref.id())
                        : Optional.empty();
                if (p.isEmpty()) {
                    p = principals.findByHandle(ref.id().toLowerCase(Locale.ROOT));
                }
                return p.filter(x -> context.existedAt(x.getCreatedAt())).map(x -> ObjectEnvelope.builder(
                        ObjectRef.of(ObjectTypes.PRINCIPAL, x.getId()))
                        .title(x.getDisplayName() + " (@" + x.getHandle() + ")")
                        .summary(x.getKind())
                        .status(x.isDisabled() ? "disabled" : "active")
                        .time(x.getCreatedAt(), null, x.getUpdatedAt())
                        .self("/v1/principals/" + x.getId())
                        .build());
            }
        };
    }

    /** {@code group:{id}}: for any token. */
    @Bean
    public ObjectResolver groupObjectResolver(GroupRepository groups, GroupMemberRepository members) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.GROUP;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                if (context.caller() == null || context.caller().anonymous()) {
                    return Optional.empty();
                }
                return groups.findById(ref.id()).filter(g -> context.existedAt(g.getCreatedAt()))
                        .map(g -> ObjectEnvelope.builder(ref)
                                .title(g.getName())
                                .summary(members.countByGroupId(g.getId()) + " members"
                                        + (g.getDescription() == null ? "" : " · " + g.getDescription()))
                                .time(g.getCreatedAt(), null, g.getUpdatedAt())
                                .self("/v1/groups/" + g.getId())
                                .build());
            }
        };
    }

    /** {@code tag:{id}}: a personal tag only for its owner; a shared tag by access list. */
    @Bean
    public ObjectResolver tagObjectResolver(TagRepository tags, ShareableObjectRepository registry) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.TAG;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return tags.findById(ref.id()).filter(t -> context.existedAt(t.getCreatedAt()))
                        .filter(t -> !t.isPersonal() || registry.findById(ref.format())
                                .map(r -> r.getOwner().equals(context.caller().principal()))
                                .orElse(t.getCreatedBy().equals(context.caller().principal())))
                        .map(t -> ObjectEnvelope.builder(ref)
                                .title(t.getName())
                                .summary(t.getDescription())
                                .status(t.getNamespace())
                                .time(t.getCreatedAt(), null, t.getUpdatedAt())
                                .self("/v1/tags/" + t.getId())
                                .build());
            }
        };
    }

    /** {@code attachment:{id}}. */
    @Bean
    public ObjectResolver attachmentObjectResolver(AttachmentRepository attachments) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.ATTACHMENT;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return attachments.findById(ref.id()).filter(a -> context.existedAt(a.getCreatedAt()))
                        .map(a -> ObjectEnvelope.builder(ref)
                                .title(a.getFilename())
                                .summary(a.getContentType() + ", " + a.getSize() + " bytes")
                                .time(a.getCreatedAt(), null, a.getCreatedAt())
                                .self("/v1/attachments/" + a.getId())
                                .build());
            }
        };
    }

    /** {@code evidence-item:{id}}: titled with its Admiralty grade. */
    @Bean
    public ObjectResolver evidenceItemObjectResolver(EvidenceItemRepository items) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.EVIDENCE_ITEM;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return items.findById(ref.id()).filter(e -> context.existedAt(e.getCreatedAt()))
                        .map(e -> ObjectEnvelope.builder(ref)
                                .title(e.getTitle())
                                .summary(e.getDescription())
                                .status(e.grade())
                                .time(e.getEventTime(), null, e.getUpdatedAt())
                                .geometry(e.getGeom())
                                .provenance(e.getCitedSourceId() == null ? java.util.List.of()
                                        : java.util.List.of(e.getCitedSourceId()), null, null)
                                .self("/v1/evidence-items/" + e.getId())
                                .build());
            }
        };
    }

    /** {@code saved-search:{id}}. */
    @Bean
    public ObjectResolver savedSearchObjectResolver(SavedSearchRepository searches) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.SAVED_SEARCH;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return searches.findById(ref.id()).filter(s -> context.existedAt(s.getCreatedAt()))
                        .map(s -> ObjectEnvelope.builder(ref)
                                .title(s.getName())
                                .summary(s.getLastMatchCount() == null ? null : s.getLastMatchCount() + " matches")
                                .status(s.isNotify() ? "notifying" : "quiet")
                                .time(s.getCreatedAt(), null, s.getUpdatedAt())
                                .self("/v1/saved-searches/" + s.getId())
                                .build());
            }
        };
    }
}

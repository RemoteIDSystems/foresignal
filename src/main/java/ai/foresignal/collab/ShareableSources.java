package ai.foresignal.collab;

import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationRepository;
import ai.foresignal.object.AnnotationRepository;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.WorkspaceRepository;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.PrincipalService;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@link ShareableSource}s of the shareable types that exist today (prompt 130):
 * <ul>
 *   <li>{@code investigation} (101): owned by the principal of the token that created it, else {@code owner};
 *       {@code instance} visibility, because investigations were instance-wide before prompt 130;</li>
 *   <li>{@code workspace} (129): its owner; {@code shared} when it has members (who keep their roles), else
 *       {@code private};</li>
 *   <li>{@code annotation} (129): its author; {@code instance} visibility (a note is visible to whoever sees its
 *       target, which {@link AccessResolver} checks first);</li>
 *   <li>{@code tag}, {@code attachment}, {@code evidence-item} and {@code saved-search}: registered by their services
 *       on creation; the defaults (creator, {@code private} or {@code instance} for shared tags) only repair a missing
 *       registration.</li>
 * </ul>
 */
@Configuration
public class ShareableSources {

    /** {@code investigation}. */
    @Bean
    public ShareableSource investigationShareableSource(InvestigationRepository investigations,
            ApiTokenRepository tokens, PrincipalService principals) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.INVESTIGATION;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                Optional<InvestigationEntity> found = investigations.findById(id);
                if (found.isEmpty() && id.startsWith("INV-")) {
                    found = investigations.findByDisplayId(id);
                }
                return found.map(i -> new Defaults(i.getId(), creator(i.getCreatedBy(), tokens, principals),
                        Visibility.INSTANCE, null));
            }
        };
    }

    /** {@code workspace}. */
    @Bean
    public ShareableSource workspaceShareableSource(WorkspaceRepository workspaces,
            org.springframework.jdbc.core.JdbcTemplate jdbc,
            org.springframework.beans.factory.ObjectProvider<AccessResolver> access) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.WORKSPACE;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return workspaces.findById(id).map(w -> new Defaults(w.getId(), w.getOwner(),
                        w.getMembers().isEmpty() ? Visibility.PRIVATE : Visibility.SHARED, null));
            }

            /**
             * A member's role, else (prompt 131) the principal's role on the chronicle that owns the workspace: editors
             * and owners of the chronicle edit its workspace, viewers and commenters view it.
             */
            @Override
            public AclRole intrinsicRole(String id, String principalId) {
                AclRole member = workspaces.findById(id).map(w -> w.getMembers().get(principalId))
                        .map(r -> "editor".equals(r) ? AclRole.EDITOR : AclRole.VIEWER).orElse(null);
                if (member != null) {
                    return member;
                }
                return chronicleRole(id, principalId, jdbc, access);
            }
        };
    }

    /** The principal's role on the chronicle owning workspace {@code workspaceId} (prompt 131), mapped, or null. */
    static AclRole chronicleRole(String workspaceId, String principalId, org.springframework.jdbc.core.JdbcTemplate jdbc,
            org.springframework.beans.factory.ObjectProvider<AccessResolver> access) {
        if (principalId == null || jdbc == null || access == null) {
            return null;
        }
        try {
            java.util.List<String> owners = jdbc.queryForList("SELECT id FROM chronicle WHERE workspace_id = ? LIMIT 1",
                    String.class, workspaceId);
            AccessResolver resolver = access.getIfAvailable();
            if (owners.isEmpty() || resolver == null) {
                return null;
            }
            String role = resolver.grantedRole("chronicle:" + owners.getFirst(), new ai.foresignal.object.ObjectCaller(
                    principalId, principalId, ai.foresignal.security.Scope.READ, false, java.util.Set.of()));
            if (role == null) {
                return null;
            }
            AclRole granted = AclRole.fromWire(role);
            return granted.atLeast(AclRole.EDITOR) ? AclRole.EDITOR : AclRole.VIEWER;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** {@code annotation}. */
    @Bean
    public ShareableSource annotationShareableSource(AnnotationRepository annotations) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.ANNOTATION;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return annotations.findById(id).filter(a -> a.getDeletedAt() == null)
                        .map(a -> new Defaults(a.getId(), a.getAuthor(), Visibility.INSTANCE, null));
            }
        };
    }

    /** {@code tag}. */
    @Bean
    public ShareableSource tagShareableSource(TagRepository tags) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.TAG;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return tags.findById(id).map(t -> new Defaults(t.getId(), t.getCreatedBy(),
                        t.isPersonal() ? Visibility.PRIVATE : Visibility.INSTANCE, null));
            }
        };
    }

    /** {@code attachment}. */
    @Bean
    public ShareableSource attachmentShareableSource(AttachmentRepository attachments) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.ATTACHMENT;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return attachments.findById(id).map(a -> new Defaults(a.getId(), a.getUploadedBy(),
                        Visibility.PRIVATE, null));
            }
        };
    }

    /** {@code evidence-item}. */
    @Bean
    public ShareableSource evidenceItemShareableSource(EvidenceItemRepository items) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.EVIDENCE_ITEM;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return items.findById(id).map(e -> new Defaults(e.getId(), e.getCreatedBy(), Visibility.PRIVATE,
                        null));
            }
        };
    }

    /** {@code saved-search}. */
    @Bean
    public ShareableSource savedSearchShareableSource(SavedSearchRepository searches) {
        return new ShareableSource() {
            @Override
            public String type() {
                return ObjectTypes.SAVED_SEARCH;
            }

            @Override
            public Optional<Defaults> defaults(String id) {
                return searches.findById(id).map(s -> new Defaults(s.getId(), s.getCreatedBy(), Visibility.PRIVATE,
                        null));
            }
        };
    }

    /**
     * The principal behind a 101 {@code created_by} actor: a principal id as is, {@code token:<name>} through the
     * oldest token of that name, else the {@code owner} principal.
     */
    static String creator(String createdBy, ApiTokenRepository tokens, PrincipalService principals) {
        if (createdBy != null && createdBy.startsWith("token:")) {
            String name = createdBy.substring("token:".length());
            Optional<ApiTokenEntity> token = tokens.findFirstByNameOrderByCreatedAtAsc(name);
            if (token.isPresent() && token.get().getPrincipalId() != null) {
                return token.get().getPrincipalId();
            }
        }
        if (createdBy != null && ai.foresignal.security.Ulids.isUlid(createdBy)
                && principals.find(createdBy).isPresent()) {
            return createdBy;
        }
        return principals.owner().getId();
    }
}

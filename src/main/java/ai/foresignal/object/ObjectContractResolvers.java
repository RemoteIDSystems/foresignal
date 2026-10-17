package ai.foresignal.object;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The resolvers of the object types prompt 129 itself owns: {@code workspace:{id}} (visible to its owner, members and
 * admins) and {@code annotation:{id}} (private annotations need private access). and {@code sensor:{kind}:{id}}
 * (through {@link SensorDiscoveryService}).
 */
@Configuration
public class ObjectContractResolvers {

    /** {@code workspace:{id}}. */
    @Bean
    public ObjectResolver workspaceObjectResolver(WorkspaceRepository workspaces, WorkspaceAccess access) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.WORKSPACE;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return workspaces.findById(ref.id()).filter(w -> access.canRead(w, context.caller()))
                        .filter(w -> context.existedAt(w.getCreatedAt()))
                        .map(w -> ObjectEnvelope.builder(ref)
                                .title(w.getName())
                                .summary("Workspace at context version " + w.getContextVersion())
                                .time(w.getCreatedAt(), null, w.getUpdatedAt())
                                .privateObject(w.isPrivateWorkspace())
                                .self("/v1/workspaces/" + w.getId())
                                .build());
            }
        };
    }

    /** {@code sensor:{kind}:{id}}. */
    @Bean
    public ObjectResolver sensorObjectResolver(SensorDiscoveryService sensors) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.SENSOR;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return sensors.resolve(ref, context);
            }
        };
    }

    /** {@code annotation:{id}}. */
    @Bean
    public ObjectResolver annotationObjectResolver(AnnotationRepository annotations) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.ANNOTATION;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return annotations.findById(ref.id()).filter(a -> a.getDeletedAt() == null)
                        .filter(a -> !a.isPrivateAnnotation() || context.seesPrivate())
                        .filter(a -> context.existedAt(a.getCreatedAt()))
                        .map(a -> ObjectEnvelope.builder(ref)
                                .title(("verdict".equals(a.getKind()) ? "Verdict " + a.getVerdict() : "Note") + " on "
                                        + a.getTargetRef())
                                .summary(a.getBody())
                                .status(a.getVerdict())
                                .time(a.getCreatedAt(), null, a.getUpdatedAt())
                                .privateObject(a.isPrivateAnnotation())
                                .self("/v1/objects/" + a.getTargetRef() + "/annotations")
                                .build());
            }
        };
    }
}

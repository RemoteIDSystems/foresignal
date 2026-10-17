package ai.foresignal.place;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.view.SavedViewService;
import ai.foresignal.watch.WatchlistService;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The resolvers of prompt 036's objects (prompt 129): {@code place:{id}}, {@code view:{id or slug}} and
 * {@code watchlist:{id}}. Places and watchlists are never anonymous (036), so they are invisible to callers without a
 * token.
 */
@Configuration
public class PlaceObjectResolvers {

    /** {@code place:{id}}. */
    @Bean
    public ObjectResolver placeObjectResolver(PlaceService places) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.PLACE;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                if (context.caller().anonymous()) {
                    return Optional.empty();
                }
                return places.find(ref.id()).filter(p -> context.existedAt(p.createdAt())).map(p -> {
                    ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                            .title(p.name())
                            .summary(p.kind() == null ? p.shape() : p.kind() + ", " + p.shape())
                            .time(p.createdAt(), null, p.updatedAt())
                            .self("/v1/places/" + p.id());
                    b.geometry(p.area() != null ? p.area() : p.geometry());
                    return b.build();
                });
            }
        };
    }

    /** {@code view:{id or slug}}. */
    @Bean
    public ObjectResolver viewObjectResolver(SavedViewService views) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.VIEW;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return views.find(ref.id()).filter(v -> context.existedAt(v.getCreatedAt())).map(v -> {
                    ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                            .title(v.getName())
                            .summary("Saved view over " + (v.getKinds().isEmpty() ? "all kinds"
                                    : String.join(", ", v.getKinds())))
                            .time(v.getCreatedAt(), null, v.getUpdatedAt())
                            .self("/v1/views/" + v.getId());
                    if (v.getArea() != null) {
                        b.geometry(v.getArea());
                    }
                    return b.build();
                });
            }
        };
    }

    /** {@code watchlist:{id}}. */
    @Bean
    public ObjectResolver watchlistObjectResolver(WatchlistService watchlists) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.WATCHLIST;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                if (context.caller().anonymous()) {
                    return Optional.empty();
                }
                ai.foresignal.watch.WatchListEntity found;
                try {
                    found = watchlists.get(ref.id());
                } catch (RuntimeException e) {
                    return Optional.empty();
                }
                return Optional.ofNullable(found).filter(w -> context.existedAt(w.getCreatedAt()))
                        .map(w -> ObjectEnvelope.builder(ref)
                                .title(w.getName())
                                .summary(w.getItemCount() + " " + w.getIdentity() + " identities")
                                .time(w.getCreatedAt(), null, w.getUpdatedAt())
                                .self("/v1/watchlists/" + w.getId())
                                .build());
            }
        };
    }
}

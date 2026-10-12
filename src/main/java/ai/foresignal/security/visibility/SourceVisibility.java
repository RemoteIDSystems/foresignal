package ai.foresignal.security.visibility;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Which sources are private, and who may see them (prompt 039, §1).
 *
 * <p><strong>Effective privacy</strong> is the descriptor's {@code private} flag (prompt 008; a custom
 * source's {@code CustomSourceInput.private} and an EDP manifest's flag become that descriptor flag,
 * 024) <em>or</em> the operator's tightening ({@code SourceSettings.private}, stored in
 * {@code app_setting['visibility']}). The operator can make any source private, but cannot make a
 * descriptor-private source public.
 *
 * <p><strong>Who sees private sources</strong> ({@link #canSeePrivate}): an {@code admin} token always;
 * a {@code read} or {@code agent} token, a feed key or a tile ticket only when created with the private
 * grant ({@code privateAccess}); an anonymous caller never. A token's own source restriction still
 * applies: a private source outside it stays hidden.
 *
 * <p>The private set is cached and refreshed on registry and {@code visibility} setting events. When a
 * source becomes private a {@link SourceBecamePrivateEvent} is published, so its embedding rows can be
 * deleted.
 */
@Component
public class SourceVisibility {

    private final SourceRegistry registry;
    private final AppSettingService settings;
    private final ApplicationEventPublisher events;
    private volatile Set<String> cached;

    /**
     * Creates the component. {@code settings} and {@code events} may be null in unit tests (no operator
     * tightening, no events).
     */
    public SourceVisibility(SourceRegistry registry, AppSettingService settings, ApplicationEventPublisher events) {
        this.registry = registry;
        this.settings = settings;
        this.events = events;
    }

    // ------------------------------------------------------------------------- which sources

    /** True when {@code sourceId} is private (descriptor or operator); an unknown source is not. */
    public boolean isPrivate(String sourceId) {
        return sourceId != null && privateSourceIds().contains(sourceId);
    }

    /** Every registered private source, sorted. */
    public Set<String> privateSourceIds() {
        Set<String> snapshot = cached;
        if (snapshot == null) {
            snapshot = compute();
            cached = snapshot;
        }
        return snapshot;
    }

    /** True when the descriptor of {@code sourceId} itself is private (the operator cannot undo this). */
    public boolean descriptorPrivate(String sourceId) {
        return registry.find(sourceId).map(r -> r.descriptor().privateSource()).orElse(false);
    }

    /** True when the operator made {@code sourceId} private ({@code SourceSettings.private}). */
    public boolean operatorPrivate(String sourceId) {
        return visibilitySettings().isPrivate(sourceId);
    }

    /**
     * Applies an {@code updateSource} {@code private} member: {@code true} tightens the source,
     * {@code false} removes the operator's tightening, null changes nothing.
     *
     * @throws ValidationException when {@code false} is asked of a descriptor-private source
     */
    @Transactional
    public void setOperatorPrivate(String sourceId, Boolean makePrivate) {
        if (makePrivate == null) {
            return;
        }
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        if (!makePrivate && descriptor.privateSource()) {
            throw new ValidationException("A private source cannot be made public.",
                    List.of(new FieldError("private", "this source is private by definition; it cannot be made public")));
        }
        VisibilitySettings current = visibilitySettings();
        if (current.isPrivate(sourceId) == makePrivate) {
            return;
        }
        settings.put(SettingKeys.VISIBILITY, current.with(sourceId, makePrivate));
    }

    /** Recomputes the cache after a registry change, announcing sources that became private. */
    @EventListener
    public void onRegistryChanged(SourceRegistryChangedEvent event) {
        refresh();
    }

    /** Recomputes the cache after the {@code visibility} setting changed. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.VISIBILITY.equals(event.key())) {
            refresh();
        }
    }

    /** Recomputes the private set now and publishes a {@link SourceBecamePrivateEvent} per new member. */
    public void refresh() {
        Set<String> before = cached;
        Set<String> after = compute();
        cached = after;
        if (before == null) {
            return;
        }
        for (String id : after) {
            if (!before.contains(id) && events != null) {
                events.publishEvent(new SourceBecamePrivateEvent(id));
            }
        }
    }

    // ------------------------------------------------------------------------- who sees them

    /**
     * True when a caller of {@code scope} (null when anonymous) with the private grant
     * {@code privateAccess} may see private sources: admin always, read and agent tokens and feed keys
     * only with the grant, anonymous never.
     */
    public static boolean canSeePrivate(Scope scope, boolean privateAccess) {
        if (scope == null) {
            return false;
        }
        return scope.includes(Scope.ADMIN) || privateAccess;
    }

    /** {@link #canSeePrivate(Scope, boolean)} for the principal a filter bound (anonymous: false). */
    public static boolean canSeePrivate(RequestPrincipal principal) {
        return principal != null && !principal.isAnonymous() && canSeePrivate(principal.scope(), principal.privateAccess());
    }

    /** {@link #canSeePrivate(Scope, boolean)} for a token (empty: anonymous, false). */
    public static boolean canSeePrivate(Optional<CurrentToken> token) {
        return token.map(t -> canSeePrivate(t.scope(), t.privateAccess())).orElse(false);
    }

    /**
     * The private sources a caller may not see: all of them unless {@link #canSeePrivate}, and those
     * outside the token's source restriction otherwise.
     *
     * @param scope the caller's scope, null when anonymous
     * @param privateAccess the caller's private grant
     * @param restrictions the token's stored restrictions, or null
     */
    public Set<String> hiddenFor(Scope scope, boolean privateAccess, TokenRestrictions restrictions) {
        Set<String> all = privateSourceIds();
        if (all.isEmpty()) {
            return Set.of();
        }
        if (!canSeePrivate(scope, privateAccess)) {
            return all;
        }
        TokenRestrictions r = TokenRestrictions.orNone(restrictions);
        if (r.sources() == null) {
            return Set.of();
        }
        Set<String> out = new TreeSet<>();
        for (String id : all) {
            if (!r.sources().contains(id)) {
                out.add(id);
            }
        }
        return java.util.Collections.unmodifiableSet(out);
    }

    /** {@link #hiddenFor} as a {@link PrivateFilter}. */
    public PrivateFilter filterFor(Scope scope, boolean privateAccess, TokenRestrictions restrictions) {
        return new PrivateFilter(hiddenFor(scope, privateAccess, restrictions));
    }

    /** The filter of a caller that may never see private sources (anonymous, system summaries). */
    public PrivateFilter publicFilter() {
        return new PrivateFilter(privateSourceIds());
    }

    /**
     * True when {@code sourceId} is visible to {@code token} (empty for an anonymous caller). An unknown
     * source is reported visible, so the caller's own "unknown source" handling applies.
     */
    public boolean visible(String sourceId, Optional<CurrentToken> token) {
        if (!isPrivate(sourceId)) {
            return true;
        }
        if (!canSeePrivate(token)) {
            return false;
        }
        TokenRestrictions r = token.map(CurrentToken::restrictions).orElse(TokenRestrictions.NONE);
        return r.sources() == null || r.sources().contains(sourceId);
    }

    /**
     * {@link #visible(String, Optional)} under a read restriction too (prompt 035): a source outside its
     * source member, or a private source when it excludes private sources, is invisible.
     */
    public boolean visible(String sourceId, Optional<CurrentToken> token, ReadRestriction restriction) {
        if (!visible(sourceId, token)) {
            return false;
        }
        if (restriction == null) {
            return true;
        }
        if (!restriction.allowsSource(sourceId)) {
            return false;
        }
        return !restriction.excludePrivate() || !isPrivate(sourceId);
    }

    /**
     * {@link #visible(String, Optional, ReadRestriction)} for a request principal (a feed key has no
     * {@link CurrentToken}; its grant is on the principal).
     */
    public boolean visible(String sourceId, Optional<CurrentToken> token, RequestPrincipal principal) {
        if (token.isEmpty() && principal != null && principal.isFeedKey()) {
            if (isPrivate(sourceId) && !canSeePrivate(principal)) {
                return false;
            }
            ReadRestriction r = principal.restriction();
            return r.allowsSource(sourceId) && (!r.excludePrivate() || !isPrivate(sourceId));
        }
        return visible(sourceId, token, principal == null ? ReadRestriction.NONE : principal.restriction());
    }

    // ------------------------------------------------------------------------- internals

    private VisibilitySettings visibilitySettings() {
        if (settings == null) {
            return VisibilitySettings.defaults();
        }
        return settings.getOrDefault(SettingKeys.VISIBILITY, VisibilitySettings.class, VisibilitySettings::defaults);
    }

    private Set<String> compute() {
        VisibilitySettings operator = visibilitySettings();
        Set<String> out = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.privateSource() || operator.isPrivate(descriptor.id())) {
                out.add(descriptor.id());
            }
        }
        return java.util.Collections.unmodifiableSet(out);
    }
}

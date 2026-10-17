package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.object.AnnotationEntity;
import ai.foresignal.object.AnnotationService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Tlp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Comment threads with mentions (prompt 130), on top of the 129 annotations: a note is a thread root, replies hang
 * under it, a root can be resolved and reopened.
 *
 * <ul>
 *   <li>Commenting on a work product needs the {@code commenter} role on it; on data and analytic objects any token
 *       comments, as in 129.</li>
 *   <li>{@code @handle} mentions notify the principals who can see the target ({@code mention}); a mention of anyone
 *       else is dropped and listed back to the author in {@code droppedMentions}.</li>
 *   <li>A reply notifies the thread's earlier participants ({@code comment.reply}).</li>
 *   <li>A note inherits the TLP label of a work product it is on (COL-2), and is visible only to those who see it.</li>
 * </ul>
 */
@Service
public class CommentService {

    /** {@code @handle}, not preceded by a word character or another {@code @} (so emails do not mention). */
    static final Pattern MENTION = Pattern.compile("(?<![\\w@.])@([a-z0-9][a-z0-9_.-]{1,31})", Pattern.CASE_INSENSITIVE);

    private final AnnotationService annotations;
    private final ObjectService objects;
    private final AccessResolver access;
    private final PrincipalRepository principals;
    private final PrincipalCallers principalCallers;
    private final PrincipalViews views;
    private final PersonalNotifier notifier;
    private final Clock clock;

    /** Creates the service. */
    public CommentService(AnnotationService annotations, ObjectService objects, AccessResolver access,
            PrincipalRepository principals, PrincipalCallers principalCallers, PrincipalViews views,
            PersonalNotifier notifier, Clock clock) {
        this.annotations = annotations;
        this.objects = objects;
        this.access = access;
        this.principals = principals;
        this.principalCallers = principalCallers;
        this.views = views;
        this.notifier = notifier;
        this.clock = clock;
    }

    /**
     * An annotation as the {@code Annotation} view with the 130 fields ({@code tlp}, {@code visibility}).
     *
     * @param a the annotation
     * @param dropped mentions dropped for the author, or null
     * @return the view
     */
    public Map<String, Object> view(AnnotationEntity a, List<String> dropped) {
        Map<String, Object> out = new LinkedHashMap<>(AnnotationService.view(a));
        Optional<ShareableObjectEntity> rec = access.record(ObjectTypes.ANNOTATION + ":" + a.getId());
        out.put("tlp", rec.map(ShareableObjectEntity::getTlp).orElse(null));
        out.put("visibility", rec.map(r -> r.getVisibility().wireValue()).orElse(null));
        if (dropped != null) {
            out.put("droppedMentions", dropped);
        }
        return out;
    }

    /**
     * Annotates {@code target} ({@code createAnnotation}): the 129 rules, plus the commenter role on a work product,
     * the TLP label and mentions.
     *
     * @return the {@code Annotation} view, with {@code droppedMentions}
     */
    @Transactional
    public Map<String, Object> create(ObjectRef target, JsonNode body, ObjectCaller caller) {
        ObjectEnvelope env = objects.get(target, new ResolveContext(caller, null));
        requireComment(env.ref(), caller);
        AnnotationEntity a = annotations.create(target, body, new ResolveContext(caller, null));
        register(a, env.ref());
        List<String> dropped = mentions(a, env, caller, null);
        return view(a, dropped);
    }

    /**
     * Replies in the thread of {@code annotationId} ({@code replyToAnnotation}).
     *
     * @return the reply's {@code Annotation} view, with {@code droppedMentions}
     */
    @Transactional
    public Map<String, Object> reply(String annotationId, JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "AnnotationReplyInput", Set.of("body", "private"));
        AnnotationEntity parent = visibleAnnotation(annotationId, caller);
        ObjectEnvelope env = objects.get(ObjectRef.parse(parent.getTargetRef()), new ResolveContext(caller, null));
        requireComment(env.ref(), caller);
        String text = CollabInputs.required(body, "body", 1, AnnotationService.MAX_BODY);
        AnnotationEntity reply = annotations.reply(parent, text, CollabInputs.bool(body, "private", false), caller);
        register(reply, env.ref());
        List<String> dropped = mentions(reply, env, caller, reply.getParentId());
        notifyParticipants(reply, env, caller);
        return view(reply, dropped);
    }

    /**
     * Resolves or reopens a thread ({@code resolveAnnotation}): commenters, editors and owners of the target may.
     *
     * @return the root's {@code Annotation} view
     */
    @Transactional
    public Map<String, Object> resolve(String annotationId, JsonNode body, ObjectCaller caller) {
        if (body != null && !body.isNull()) {
            CollabInputs.object(body, "AnnotationResolveInput", Set.of("resolved"));
        }
        AnnotationEntity a = visibleAnnotation(annotationId, caller);
        AnnotationEntity root = a.getParentId() == null ? a : visibleAnnotation(a.getParentId(), caller);
        ObjectEnvelope env = objects.get(ObjectRef.parse(root.getTargetRef()), new ResolveContext(caller, null));
        requireComment(env.ref(), caller);
        return view(annotations.setResolved(root, CollabInputs.bool(body, "resolved", true)), null);
    }

    /**
     * The comment threads of {@code target} ({@code listObjectAnnotations?thread=true}).
     *
     * @return the page of {@code AnnotationThread}s
     */
    @Transactional
    public Map<String, Object> threads(ObjectRef target, String cursor, int limit, ObjectCaller caller) {
        return annotations.threads(target, cursor, limit, new ResolveContext(caller, null), a -> view(a, null));
    }

    /**
     * The annotation {@code id} when the caller sees it and its target.
     *
     * @throws ai.foresignal.api.error.NotFoundException {@code UNKNOWN_ANNOTATION} otherwise
     */
    public AnnotationEntity visibleAnnotation(String id, ObjectCaller caller) {
        AnnotationEntity a = annotations.get(id, caller);
        if (access.role(caller, ObjectTypes.ANNOTATION + ":" + a.getId()) == null) {
            throw new ai.foresignal.api.error.NotFoundException(ErrorCodes.UNKNOWN_ANNOTATION,
                    "No annotation '" + id + "' exists, or you may not see it.");
        }
        return a;
    }

    /** Requires the commenter role on a work product target; any token comments on other objects. */
    private void requireComment(String targetRef, ObjectCaller caller) {
        if (AccessResolver.isShareable(targetRef) && !ObjectTypes.ANNOTATION.equals(targetRef.substring(0,
                targetRef.indexOf(':'))) && !access.can(caller, targetRef, AccessAction.COMMENT)) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Commenting on " + targetRef + " needs the commenter role.");
        }
    }

    /** Registers a note: its author owns it; it inherits the TLP label of a work product it is on. */
    private void register(AnnotationEntity a, String targetRef) {
        Tlp tlp = AccessResolver.isShareable(targetRef) ? access.tlpOf(targetRef) : null;
        access.register(ObjectTypes.ANNOTATION + ":" + a.getId(), a.getAuthor(), Visibility.INSTANCE, tlp);
    }

    /**
     * Notifies the principals {@code @mentioned} in {@code a} who can see the target, records them on the
     * annotation, and returns the handles that were dropped.
     */
    List<String> mentions(AnnotationEntity a, ObjectEnvelope target, ObjectCaller author, String parentId) {
        Set<String> handles = parse(a.getBody());
        List<String> notified = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        Map<String, Object> authorView = views.summary(author.principal());
        Instant now = clock.instant();
        for (String handle : handles) {
            Optional<PrincipalEntity> p = principals.findByHandle(handle);
            if (p.isEmpty() || !canSee(p.get().getId(), target)) {
                dropped.add(handle);
                continue;
            }
            notified.add(handle);
            if (p.get().getId().equals(author.principal())) {
                continue;
            }
            notifier.send(new PersonalNotifier.Notice(p.get().getId(), MeEvents.MENTION,
                    display(authorView) + " mentioned you on " + target.title(), a.getBody(), target.ref(),
                    author.principal(), tlpOf(target.ref()), MeEvents.comment("mention", a.getId(), parentId,
                            target.ref(), authorView, a.getBody(), now)));
        }
        if (!notified.isEmpty()) {
            annotations.setMentions(a, notified);
        }
        return dropped;
    }

    private void notifyParticipants(AnnotationEntity reply, ObjectEnvelope target, ObjectCaller author) {
        Set<String> participants = new LinkedHashSet<>();
        annotations.find(reply.getParentId()).ifPresent(root -> participants.add(root.getAuthor()));
        for (AnnotationEntity r : annotations.replies(reply.getParentId())) {
            participants.add(r.getAuthor());
        }
        participants.remove(author.principal());
        Set<String> mentioned = new LinkedHashSet<>();
        for (String handle : reply.getMentions()) {
            principals.findByHandle(handle).ifPresent(p -> mentioned.add(p.getId()));
        }
        Map<String, Object> authorView = views.summary(author.principal());
        Instant now = clock.instant();
        for (String p : participants) {
            if (mentioned.contains(p) || !canSee(p, target)) {
                continue;
            }
            notifier.send(new PersonalNotifier.Notice(p, MeEvents.COMMENT_REPLY, display(authorView)
                    + " replied on " + target.title(), reply.getBody(), target.ref(), author.principal(),
                    tlpOf(target.ref()), MeEvents.comment("reply", reply.getId(), reply.getParentId(), target.ref(),
                            authorView, reply.getBody(), now)));
        }
    }

    /** True when principal {@code id} can see {@code target} (its access list, or its scope and private rule). */
    private boolean canSee(String id, ObjectEnvelope target) {
        Optional<ObjectCaller> as = principalCallers.of(id);
        if (as.isEmpty()) {
            return false;
        }
        if (AccessResolver.isShareable(target.ref())) {
            return access.role(as.get(), target.ref()) != null;
        }
        return !target.privateObject() || as.get().seesPrivate();
    }

    private String tlpOf(String ref) {
        return AccessResolver.isShareable(ref) ? access.tlpOf(ref).wireValue() : null;
    }

    /** The distinct lower-case handles mentioned in {@code body}. */
    static Set<String> parse(String body) {
        Set<String> out = new LinkedHashSet<>();
        if (body == null) {
            return out;
        }
        Matcher m = MENTION.matcher(body);
        while (m.find()) {
            String handle = m.group(1).toLowerCase(Locale.ROOT);
            while (handle.endsWith(".") || handle.endsWith("-")) {
                handle = handle.substring(0, handle.length() - 1);
            }
            if (handle.length() >= 2) {
                out.add(handle);
            }
        }
        return out;
    }

    private static String display(Map<String, Object> summary) {
        return summary == null ? "Someone" : String.valueOf(summary.get("displayName"));
    }
}

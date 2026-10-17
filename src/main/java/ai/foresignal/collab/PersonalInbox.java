package ai.foresignal.collab;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The per-principal view of the inbox ({@code GET /v1/inbox}, prompt 130): mentions, shares, replies, saved-search
 * matches and the other items addressed to the caller, newest first. Listing an item marks it read (the response
 * shows whether it was read before); reading never acknowledges an alert. An item whose object the caller can no
 * longer see keeps its title but has no envelope.
 */
@Service
public class PersonalInbox {

    /** The categories of {@code InboxItem}. */
    public static final List<String> CATEGORIES = List.of("mention", "share", "report", "chronicle", "assessment",
            "comment-reply", "access-request", "saved-search", "system");

    private final InboxItemRepository items;
    private final ObjectService objects;
    private final PrincipalViews views;
    private final EntityManager entities;
    private final Clock clock;

    /** Creates the service. */
    public PersonalInbox(InboxItemRepository items, ObjectService objects, PrincipalViews views,
            EntityManager entities, Clock clock) {
        this.items = items;
        this.objects = objects;
        this.views = views;
        this.entities = entities;
        this.clock = clock;
    }

    /**
     * One page of the caller's inbox, newest first.
     *
     * @param caller the caller (a token)
     * @param unread only unread items
     * @param category a category, or null
     * @param since only items created at or after, or null
     * @param cursor the last id of the previous page, or null
     * @param limit the page size
     * @return the {@code InboxPage}
     */
    @Transactional
    public Map<String, Object> list(ObjectCaller caller, boolean unread, String category, Instant since,
            String cursor, int limit) {
        if (category != null && !CATEGORIES.contains(category)) {
            throw ValidationException.forField("category", "must be one of " + CATEGORIES);
        }
        Long before = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                before = Long.parseLong(cursor.strip());
            } catch (NumberFormatException e) {
                throw ValidationException.forField("cursor", "is not a cursor this endpoint issued");
            }
        }
        StringBuilder jpql = new StringBuilder("select i from InboxItemEntity i where i.principalId = :p");
        if (unread) {
            jpql.append(" and i.readAt is null");
        }
        if (category != null) {
            jpql.append(" and i.category = :category");
        }
        if (since != null) {
            jpql.append(" and i.createdAt >= :since");
        }
        if (before != null) {
            jpql.append(" and i.id < :before");
        }
        jpql.append(" order by i.id desc");
        TypedQuery<InboxItemEntity> q = entities.createQuery(jpql.toString(), InboxItemEntity.class)
                .setParameter("p", caller.principal()).setMaxResults(limit + 1);
        if (category != null) {
            q.setParameter("category", category);
        }
        if (since != null) {
            q.setParameter("since", since);
        }
        if (before != null) {
            q.setParameter("before", before);
        }
        List<InboxItemEntity> rows = q.getResultList();
        boolean more = rows.size() > limit;
        List<InboxItemEntity> page = more ? rows.subList(0, limit) : rows;
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        List<Map<String, Object>> out = new ArrayList<>();
        Instant now = clock.instant();
        ResolveContext context = new ResolveContext(caller, null);
        for (InboxItemEntity item : page) {
            out.add(view(item, context, cache));
            if (item.getReadAt() == null) {
                item.setReadAt(now);
                items.save(item);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", out);
        result.put("nextCursor", more ? String.valueOf(page.get(page.size() - 1).getId()) : null);
        result.put("unread", items.countByPrincipalIdAndReadAtIsNull(caller.principal()));
        return result;
    }

    private Map<String, Object> view(InboxItemEntity item, ResolveContext context,
            Map<String, Map<String, Object>> cache) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", item.getId());
        out.put("event", item.getEvent());
        out.put("category", item.getCategory());
        out.put("severity", item.getSeverity());
        out.put("title", item.getTitle());
        out.put("body", item.getBody());
        out.put("ref", item.getObjectRef());
        ObjectEnvelope envelope = null;
        if (item.getObjectRef() != null && ObjectRef.isValid(item.getObjectRef())) {
            envelope = objects.find(ObjectRef.parse(item.getObjectRef()), context).orElse(null);
        }
        out.put("envelope", envelope);
        out.put("actor", views.summary(item.getActorId(), cache));
        out.put("tlp", item.getTlp());
        out.put("createdAt", item.getCreatedAt());
        out.put("read", item.getReadAt() != null);
        return out;
    }
}

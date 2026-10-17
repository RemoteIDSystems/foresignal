package ai.foresignal.object;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads relations touching a set of objects, valid at a time ({@code asOf}, or now): the validity interval contains
 * the time, the relation was written by then and not retired by then. Private relations are left out for callers
 * without private access.
 */
@Component
public class RelationQuery {

    private final EntityManager em;

    /** Creates the query. */
    public RelationQuery(EntityManager em) {
        this.em = em;
    }

    /**
     * Relations touching {@code refs}.
     *
     * @param refs the object references
     * @param types relation types to keep (empty for all)
     * @param direction {@code out} (from the refs), {@code in} (to them) or {@code both}
     * @param at the validity time
     * @param asOf true when {@code at} is a knowledge cutoff (relations written later are left out)
     * @param minConfidence the lowest confidence kept
     * @param seesPrivate true when private relations may be returned
     * @param limit the most rows returned
     * @return the relations, ordered by id
     */
    @Transactional(readOnly = true)
    public List<RelationEntity> touching(Collection<String> refs, Set<String> types, String direction, Instant at,
            boolean asOf, double minConfidence, boolean seesPrivate, int limit) {
        if (refs.isEmpty()) {
            return List.of();
        }
        StringBuilder q = new StringBuilder("select r from RelationEntity r where ");
        switch (direction == null ? "both" : direction) {
            case "out" -> q.append("r.fromRef in :refs");
            case "in" -> q.append("r.toRef in :refs");
            default -> q.append("(r.fromRef in :refs or r.toRef in :refs)");
        }
        q.append(" and r.validFrom <= :at and (r.validTo is null or r.validTo > :at)");
        q.append(" and (r.retiredAt is null or r.retiredAt > :at)");
        if (asOf) {
            q.append(" and r.createdAt <= :at");
        }
        if (!types.isEmpty()) {
            q.append(" and r.type in :types");
        }
        if (minConfidence > 0) {
            q.append(" and r.confidence >= :minConfidence");
        }
        if (!seesPrivate) {
            q.append(" and r.privateRelation = false");
        }
        q.append(" order by r.id");
        TypedQuery<RelationEntity> query = em.createQuery(q.toString(), RelationEntity.class)
                .setParameter("refs", refs).setParameter("at", at).setMaxResults(limit);
        if (!types.isEmpty()) {
            query.setParameter("types", types);
        }
        if (minConfidence > 0) {
            query.setParameter("minConfidence", minConfidence);
        }
        return query.getResultList();
    }
}

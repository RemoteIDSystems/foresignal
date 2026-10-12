package ai.foresignal.inbound.format;

/**
 * One inbound item a format refused before it became a record. A rejection never fails the request.
 *
 * @param index the item's position in the request as sent (0-based)
 * @param externalId the item's external id when one could be read, else null
 * @param reason a stable code such as {@code EXTERNAL_ID_INVALID}, {@code UNKNOWN_KIND},
 *     {@code MISSING_OBSERVED_AT} or {@code INVALID_ITEM}
 * @param detail a human-readable explanation for logs; not returned to callers
 */
public record ItemRejection(int index, String externalId, String reason, String detail) {}

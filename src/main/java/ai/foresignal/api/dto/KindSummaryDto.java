package ai.foresignal.api.dto;

import ai.foresignal.source.spi.KindDisplay;
import java.util.List;

/**
 * {@code KindSummary}: one kind currently present.
 *
 * @param kind the kind
 * @param currentCount current (not expired, not tombstoned) records of it
 * @param sources the sources emitting it
 * @param display the declared {@code kindDisplay}, or null
 */
public record KindSummaryDto(String kind, long currentCount, List<String> sources, KindDisplay display) {}

package ai.elaro.store;

/**
 * How many current records exist of one kind.
 *
 * @param kind the record kind, for example {@code earthquake}
 * @param count number of {@code record_head} rows with that kind
 */
public record KindCount(String kind, long count) {}

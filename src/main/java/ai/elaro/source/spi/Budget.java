package ai.elaro.source.spi;

/**
 * How many upstream requests a source may spend.
 *
 * <p>Elaro refuses rather than borrowing against a budget, which is what turns "a good upstream
 * citizen" into a property of the system instead of an intention.
 *
 * @param perMinute requests per minute
 * @param perDay requests per UTC day
 */
public record Budget(int perMinute, int perDay) {

    public static Budget of(int perMinute, int perDay) {
        return new Budget(perMinute, perDay);
    }
}

/**
 * Helpers shared by several adapter families that are neither SPI nor a format parser (prompt 072): for example
 * {@link ai.foresignal.source.support.AbsenceTombstoner}, which the catalog families (072, 074, 075) use to retire
 * entities that disappear from complete fetches.
 *
 * <p>Adapters may import this package; it never imports an adapter.
 */
package ai.foresignal.source.support;

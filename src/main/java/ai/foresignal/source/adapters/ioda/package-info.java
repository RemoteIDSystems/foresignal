/**
 * The IODA internet-outage source {@code ioda-outages} (prompt 068): IODA's outage alerts for the last window,
 * folded into one {@code outage} state record per entity and datasource whose {@code level} flips between
 * {@code critical} and {@code normal}, placed at 040's country and admin-1 label points without any geocoding, and
 * {@code LOCAL_ONLY} because IODA publishes no terms.
 */
package ai.foresignal.source.adapters.ioda;

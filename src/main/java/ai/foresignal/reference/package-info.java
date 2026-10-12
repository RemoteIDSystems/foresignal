/**
 * Reference geography and region tagging (prompt 040).
 *
 * <p>{@link ai.foresignal.reference.CountryCentroids} is a bundled country table (Natural Earth admin-0
 * label points and aliases) that works with no network and no database rows. The
 * {@link ai.foresignal.reference.ReferenceDataLoader} downloads the datasets named in the bundled
 * {@code reference/manifest.json} (Natural Earth boundaries and sea areas, GeoNames places) through the
 * budgeted upstream client, verifies their SHA-256 and swaps them into the {@code ref_*} and
 * {@code gazetteer_*} tables. The {@link ai.foresignal.reference.RegionTagger} turns a geometry into
 * {@link ai.foresignal.reference.RegionTags} (country, first-level region, US county, sea and the
 * nearest place), and the {@link ai.foresignal.reference.RegionTagWorker} keeps {@code record_region}
 * in step with the record store. Tags live beside records, never in their payload, so they are outside
 * every content hash and never create versions (Core-Data-Model §4).
 */
package ai.foresignal.reference;

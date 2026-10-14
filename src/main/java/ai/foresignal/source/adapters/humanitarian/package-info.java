/**
 * The humanitarian and disaster sources (prompt 090): {@code nasa-eonet}, {@code reliefweb-disasters},
 * {@code unhcr-population}, {@code who-don}, {@code copernicus-ems}, the volcano-alert sources
 * ({@code usgs-hans-volcanoes}, {@code geonet-volcanoes}), {@code openfema-declarations}, the travel
 * advisories and the optional {@code hdx-hapi-*} family. Most records are country-level: geometry comes from
 * prompt 040's gazetteer (a label point or polygon) and is flagged {@code geometryApproximate=true}.
 *
 * <p>All free text from these sources is untrusted for LLM use (prompt 028): it is quoted as data, never as
 * instructions.
 */
package ai.foresignal.source.adapters.humanitarian;

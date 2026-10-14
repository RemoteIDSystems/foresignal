/**
 * The iNaturalist source {@code inat-observations} (prompt 095): recent observations in an operator's area from
 * the v1 observations API, as {@code nature-observation} point records. An AOI is required (no global polling);
 * obscured and geoprivacy-private observations keep their randomised upstream coordinates, and contributor
 * usernames, descriptions and comments are never stored. v2 is preferred upstream and is added behind the
 * {@code ApiVersion} seam.
 */
package ai.foresignal.source.adapters.inat;

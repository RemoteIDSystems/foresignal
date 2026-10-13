/**
 * The SatDump ground station source family (prompt 049, Weather-Imagery-Design §4).
 *
 * <p>One {@code satdump-{station}} source per configured station owns {@code ground-station},
 * {@code satellite-pass} and {@code raster-frame}: an optional {@code /api} loop (station state, RF
 * sessions, live polar plot) and an inbox loop over SatDump's output directory (passes, products,
 * thumbnails, GOES and Elektro xRIT geostationary frames).
 *
 * <p>SatDump is GPL-3.0 and runs as a separate, operator-supplied process. Foresignal talks to it only
 * through HTTP and files: no SatDump source, headers, structs or projection code is here, and every
 * parser was written from observed output and fixtures. The geostationary maths comes from the public
 * CGMS and GOES-R PUG specifications.
 */
package ai.foresignal.source.adapters.satdump;

/**
 * The {@code cctv-*} source family (prompt 072): public traffic-camera <strong>catalogs</strong> (location,
 * direction, image or stream URL, access hints) from official open-data endpoints, one source per reviewed pack of
 * {@link ai.foresignal.source.adapters.cctv.CctvPacks}.
 *
 * <p>Frames are never fetched, proxied or stored: a {@code camera} record carries the upstream frame URL and a
 * {@code frameAccess} hint, and clients load the frame from the upstream host themselves. Pack lists are code-reviewed
 * and never caller-supplied; every frame URL must be on one of its pack's allowed origins.
 */
package ai.foresignal.source.adapters.cctv;

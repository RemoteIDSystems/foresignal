/**
 * The shared readsb aircraft JSON mapping (prompt 062; reused by prompts 069 and 080) and the shared
 * descriptor defaults of the global aircraft sources.
 *
 * <p>Shared format code lives under {@code ai.foresignal.source.formats.*} rather than under
 * {@code adapters}, so the adsb.lol, OpenSky and local receiver adapters can import it without breaking
 * the no-cross-adapter rule (Conventions, "Shared parsers"; Adapter Guide §1).
 *
 * <ul>
 *   <li>{@link ai.foresignal.source.formats.readsb.ReadsbDocument}: the top level of an adsb.lol
 *       ({@code ac}) or local readsb ({@code aircraft}) response;
 *   <li>{@link ai.foresignal.source.formats.readsb.ReadsbAircraftMapper}: one aircraft entry to one
 *       record, with the Core-Data-Model §2 time base {@code observedAt = now − seen};
 *   <li>{@link ai.foresignal.source.formats.readsb.AircraftFields}: the payload keys and unit
 *       conversions every aircraft source shares, OpenSky included;
 *   <li>{@link ai.foresignal.source.formats.readsb.AircraftDescriptors}: the §5.3 SAMPLED TRACK defaults
 *       and the {@code fieldSpecs} of those keys.
 * </ul>
 *
 * <p>Ported in part from God's Eye View (MIT): {@code src/data/adsbLolFallback.js} (the readsb field
 * mapping and {@code now} in seconds or milliseconds); see {@code THIRD_PARTY_NOTICES.md}.
 */
package ai.foresignal.source.formats.readsb;

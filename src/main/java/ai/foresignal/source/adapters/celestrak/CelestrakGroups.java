package ai.foresignal.source.adapters.celestrak;

// Origin: the group-name pattern ^[a-z0-9-]+$ follows God's Eye View (MIT, Copyright (c) 2026 Bilawal Sidhu):
// server/providers/space/celestrak.js (celestrakProxy). See THIRD_PARTY_NOTICES.md.

import java.util.List;
import java.util.regex.Pattern;

/**
 * The CelesTrak GP groups {@code celestrak-gp} may request (prompt 064): a reviewed allowlist copied from the
 * group links of {@code https://celestrak.org/NORAD/elements/} on 2026-10-13. {@code iridium-NEXT} is the one
 * listed group left out: it does not match {@value #PATTERN_TEXT}.
 */
public final class CelestrakGroups {

    /** The shape of a group name. */
    public static final String PATTERN_TEXT = "^[a-z0-9-]+$";

    /** {@link #PATTERN_TEXT} compiled. */
    public static final Pattern PATTERN = Pattern.compile(PATTERN_TEXT);

    /** Every object CelesTrak publishes. */
    public static final String ACTIVE = "active";

    /** The allowlist, sorted. */
    public static final List<String> ALLOWED = List.of("active", "amateur", "analyst", "argos", "beidou",
            "cosmos-2251-debris", "cubesat", "dmc", "education", "engineering", "eutelsat", "fengyun-1c-debris",
            "galileo", "geo", "geodetic", "globalstar", "glo-ops", "gnss", "goes", "gps-ops", "hulianwang", "intelsat",
            "iridium-33-debris", "kuiper", "last-30-days", "military", "noaa", "oneweb", "orbcomm", "other-comm",
            "planet", "qianfan", "radar", "resource", "sar", "sarsat", "satnogs", "sbas", "science", "ses", "spire",
            "starlink", "stations", "tdrss", "telesat", "visual", "weather", "x-comm");

    /** The member groups fetched by default, each about once a day (prompt 107 adds noaa and goes). */
    public static final List<String> DEFAULT_MEMBERS = List.of("stations", "visual", "weather", "noaa", "goes",
            "amateur", "gnss", "geo", "science", "cubesat", "last-30-days");

    private CelestrakGroups() {}

    /** True when {@code group} has the right shape and is on the allowlist. */
    public static boolean allowed(String group) {
        return group != null && PATTERN.matcher(group).matches() && ALLOWED.contains(group);
    }
}

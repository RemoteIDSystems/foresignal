package ai.foresignal.source.adapters.roads;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * One reviewed Open511 server (prompt 074): an {@code open511-*} source.
 *
 * @param id the source id, {@code open511-…}
 * @param name the display name
 * @param jurisdiction who publishes the events
 * @param eventsUrl the {@code /events} URL with its query (format, status, limit)
 * @param homepage the publisher's page
 * @param attribution the attribution text
 * @param license the license name
 * @param termsUrl the license or terms page
 * @param language the language of the events' text
 */
public record Open511Server(String id, String name, String jurisdiction, URI eventsUrl, String homepage,
        String attribution, String license, String termsUrl, String language) {

    private static final Pattern ID = Pattern.compile("^open511-[a-z0-9]+(-[a-z0-9]+)*$");

    /** The servers that ship (all disabled). */
    public static final List<Open511Server> REVIEWED = List.of(new Open511Server(
            "open511-drivebc",
            "DriveBC road events (Open511)",
            "British Columbia Ministry of Transportation and Transit (DriveBC)",
            URI.create("https://api.open511.gov.bc.ca/events?format=json&status=ACTIVE&limit=500"),
            "https://api.open511.gov.bc.ca/help",
            "Contains information licensed under the Open Government Licence – British Columbia (DriveBC)",
            "Open Government Licence – British Columbia",
            "https://www2.gov.bc.ca/gov/content/data/policy-standards/open-data/open-government-licence-bc",
            "en"));

    /** This server with its events URL moved under a test base URL ({@link RoadFetch#rebased}). */
    public Open511Server rebased(String base) {
        return new Open511Server(id, name, jurisdiction, RoadFetch.rebased(eventsUrl, base), homepage, attribution,
                license, termsUrl, language);
    }

    /**
     * The problems of a server list: ids, https URLs on the standard {@code /events} route, texts.
     *
     * @return the problems; empty when the list is valid
     */
    public static List<String> problems(List<Open511Server> servers) {
        List<String> out = new ArrayList<>();
        Set<String> ids = new java.util.HashSet<>();
        for (Open511Server s : servers) {
            if (s.id() == null || !ID.matcher(s.id()).matches()) {
                out.add(s.id() + ": the id must match " + ID.pattern());
            } else if (!ids.add(s.id())) {
                out.add(s.id() + ": duplicate id");
            }
            URI url = s.eventsUrl();
            if (url == null || !"https".equals(url.getScheme()) || url.getRawUserInfo() != null
                    || url.getPath() == null || !url.getPath().endsWith("/events")) {
                out.add(s.id() + ": the events URL must be https on the /events route");
            }
            for (String text : new String[] {s.name(), s.jurisdiction(), s.homepage(), s.attribution(), s.license(),
                    s.termsUrl(), s.language()}) {
                if (text == null || text.isBlank()) {
                    out.add(s.id() + ": every text field is required");
                    break;
                }
            }
        }
        return out;
    }
}

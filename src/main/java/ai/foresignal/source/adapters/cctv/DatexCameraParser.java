/*
 * The DATEX II mapping (predefinedLocation id → name and point; trafficView location reference → image URL) is
 * ported from God's Eye View, server/providers/cctv/sources.js (parseTarkteeDatexLocations,
 * parseTarkteeDatexImages, loadTarkteeSourcesFromDatex),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: a hardened StAX reader replaces the regular expressions; the location id is the camera id; a location
 * without a current image is kept offline instead of dropped; the image-origin pin moved to CameraRecordMapper.
 */
package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * The Transpordiamet (Tarktee) DATEX II pack parser (prompt 072): {@code roadCameraLocations} (the first catalog URL)
 * joined with {@code roadCameraImages} (the second) by location id.
 *
 * <p>XML is read with {@link CapParser#secureInputFactory()}: no DTD, no external entities, no entity replacement.
 */
public final class DatexCameraParser {

    /** A location of {@code roadCameraLocations}. */
    record Location(String name, Double lat, Double lon) {}

    private DatexCameraParser() {}

    /** Parses the locations document ({@code body}) and the images document ({@code pc.extraBody(0)}). */
    public static List<CameraEntry> estonia(byte[] body, PackContext pc) {
        Map<String, Location> locations = locations(body, pc);
        Map<String, String> images = images(pc.extraBody(0), pc);
        List<CameraEntry> out = new ArrayList<>();
        for (Map.Entry<String, Location> e : locations.entrySet()) {
            Location loc = e.getValue();
            String image = images.get(e.getKey());
            out.add(CameraEntry.builder(e.getKey(), loc.lat(), loc.lon())
                    .name(loc.name() == null ? "Tarktee " + e.getKey() : loc.name())
                    .active(image != null)
                    .imageUrl(image)
                    .build());
        }
        return out;
    }

    /** {@code predefinedLocation id} → name and point, in document order; a repeated id is counted once. */
    static Map<String, Location> locations(byte[] xml, PackContext pc) {
        Map<String, Location> out = new LinkedHashMap<>();
        XMLStreamReader r = open(xml, pc);
        try {
            String id = null;
            String name = null;
            Double lat = null;
            Double lon = null;
            boolean seenRoot = false;
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String local = r.getLocalName();
                    if (!seenRoot) {
                        seenRoot = true;
                        if (!"d2LogicalModel".equals(local)) {
                            throw CctvJsonParsers.format(pc, "is not a DATEX II document");
                        }
                    }
                    switch (local) {
                        case "predefinedLocation" -> {
                            id = r.getAttributeValue(null, "id");
                            name = null;
                            lat = null;
                            lon = null;
                        }
                        case "value" -> {
                            if (id != null && name == null) {
                                name = blankToNull(r.getElementText());
                            }
                        }
                        case "latitude" -> {
                            if (id != null) {
                                lat = parse(r.getElementText());
                            }
                        }
                        case "longitude" -> {
                            if (id != null) {
                                lon = parse(r.getElementText());
                            }
                        }
                        default -> { }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT && "predefinedLocation".equals(r.getLocalName())) {
                    if (id == null || id.isBlank()) {
                        pc.skip(PackContext.MALFORMED);
                    } else if (out.containsKey(id)) {
                        pc.skip(PackContext.DUPLICATE);
                    } else {
                        out.put(id, new Location(name, lat, lon));
                    }
                    id = null;
                }
            }
            if (!seenRoot) {
                throw CctvJsonParsers.format(pc, "is empty");
            }
        } catch (XMLStreamException e) {
            throw CctvJsonParsers.format(pc, "is not well-formed XML");
        } finally {
            close(r);
        }
        return out;
    }

    /** Location reference id → the first image URL of its {@code trafficView}. */
    static Map<String, String> images(byte[] xml, PackContext pc) {
        Map<String, String> out = new LinkedHashMap<>();
        XMLStreamReader r = open(xml, pc);
        try {
            boolean inView = false;
            String ref = null;
            String url = null;
            boolean seenRoot = false;
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String local = r.getLocalName();
                    if (!seenRoot) {
                        seenRoot = true;
                        if (!"d2LogicalModel".equals(local)) {
                            throw CctvJsonParsers.format(pc, "images document is not DATEX II");
                        }
                    }
                    switch (local) {
                        case "trafficView" -> {
                            inView = true;
                            ref = null;
                            url = null;
                        }
                        case "linearPredefinedLocationReference" -> {
                            if (inView && ref == null) {
                                ref = r.getAttributeValue(null, "id");
                            }
                        }
                        case "urlLinkAddress" -> {
                            if (inView && url == null) {
                                url = blankToNull(r.getElementText());
                            }
                        }
                        default -> { }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT && "trafficView".equals(r.getLocalName())) {
                    if (ref != null && url != null) {
                        out.putIfAbsent(ref, url);
                    }
                    inView = false;
                }
            }
            if (!seenRoot) {
                throw CctvJsonParsers.format(pc, "images document is empty");
            }
        } catch (XMLStreamException e) {
            throw CctvJsonParsers.format(pc, "images document is not well-formed XML");
        } finally {
            close(r);
        }
        return out;
    }

    private static XMLStreamReader open(byte[] xml, PackContext pc) {
        try {
            return CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(xml));
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException(pc.pack().id() + ": the catalog is not XML", "", e);
        }
    }

    private static void close(XMLStreamReader r) {
        try {
            r.close();
        } catch (XMLStreamException ignored) {
            // Nothing to release beyond the in-memory stream.
        }
    }

    private static Double parse(String text) {
        try {
            double d = Double.parseDouble(text.strip());
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}

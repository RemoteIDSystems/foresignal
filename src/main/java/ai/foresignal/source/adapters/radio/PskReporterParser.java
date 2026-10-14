package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * One PSK Reporter {@code /query} response as {@code radio-spot} records (prompt 082). Pure.
 *
 * <ul>
 *   <li><strong>XML:</strong> read with a hardened StAX reader ({@link CapParser#secureInputFactory()}:
 *       {@code SUPPORT_DTD=false}, {@code IS_SUPPORTING_EXTERNAL_ENTITIES=false}, no external resolution). A document
 *       that contains a DOCTYPE or an entity reference throws {@link UpstreamFormatException}.
 *   <li><strong>Reports:</strong> each {@code receptionReport} with {@code senderCallsign, receiverCallsign,
 *       frequency} and {@code flowStartSeconds} becomes one record; others are skipped and counted.
 *       {@code externalId} = {@code sender|receiver|frequency/1000|flowStartSeconds} (kHz, integer division), and
 *       a repeated id within one response is kept once. At most {@code cap} records.
 *   <li><strong>Time:</strong> {@code eventTime = observedAt = flowStartSeconds}.
 *   <li><strong>Geometry:</strong> the center of the sender's locator cell (a 10-character locator is read to its
 *       8-character prefix); no geometry without a valid locator. {@code quality.accuracyMeters} is half the cell's
 *       diagonal.
 *   <li><strong>Payload:</strong> {@code sender, senderLocator, mode, frequencyHz, band, snr, dxcc, receiverLocator}.
 *       The receiver's call sign is never in the payload: it is the record's {@link Observer} ({@code receiverId},
 *       {@code frequencyHz}, {@code snr}), served only under {@code include=observer}.
 *   <li><strong>Coverage:</strong> the number of {@code activeReceiver} elements (present only when the query is not
 *       {@code rronly=1}).
 * </ul>
 */
public final class PskReporterParser {

    /** The record kind. */
    public static final String KIND = "radio-spot";

    /** A report without one of the four identifying attributes. */
    static final String MISSING_FIELDS = "missing-fields";

    /** A report whose number attributes did not parse. */
    static final String BAD_NUMBER = "bad-number";

    /** A report repeating an id already seen in the response. */
    static final String DUPLICATE = "duplicate-id";

    /** Amateur bands by frequency range in Hz (ITU Region 2 edges, wide enough for every region). */
    private static final Object[][] BANDS = {
        {"2200m", 135_700L, 137_800L}, {"630m", 472_000L, 479_000L}, {"160m", 1_800_000L, 2_000_000L},
        {"80m", 3_500_000L, 4_000_000L}, {"60m", 5_250_000L, 5_450_000L}, {"40m", 7_000_000L, 7_300_000L},
        {"30m", 10_100_000L, 10_150_000L}, {"20m", 14_000_000L, 14_350_000L}, {"17m", 18_068_000L, 18_168_000L},
        {"15m", 21_000_000L, 21_450_000L}, {"12m", 24_890_000L, 24_990_000L}, {"10m", 28_000_000L, 29_700_000L},
        {"6m", 50_000_000L, 54_000_000L}, {"4m", 70_000_000L, 71_000_000L}, {"2m", 144_000_000L, 148_000_000L},
        {"1.25m", 222_000_000L, 225_000_000L}, {"70cm", 420_000_000L, 450_000_000L},
        {"33cm", 902_000_000L, 928_000_000L}, {"23cm", 1_240_000_000L, 1_300_000_000L}
    };

    private PskReporterParser() {}

    /**
     * The parse result.
     *
     * @param records the spots, at most the cap
     * @param reports every {@code receptionReport} element seen
     * @param activeReceivers the {@code activeReceiver} elements seen
     * @param capped true when reports were dropped at the cap
     * @param skipped skip reason to count
     */
    public record Result(List<NormalizedRecord> records, int reports, int activeReceivers, boolean capped,
            Map<String, Integer> skipped) {

        /** Copies the collections. */
        public Result {
            records = List.copyOf(records);
            skipped = Map.copyOf(skipped);
        }
    }

    /**
     * Parses a response.
     *
     * @param body the XML body
     * @param cap the most records returned
     * @throws UpstreamFormatException when the body is not a {@code receptionReports} document, or contains a
     *     DOCTYPE or entity reference
     */
    public static Result parse(byte[] body, int cap) {
        Map<String, NormalizedRecord> byId = new LinkedHashMap<>();
        Map<String, Integer> skipped = new TreeMap<>();
        int reports = 0;
        int receivers = 0;
        boolean capped = false;
        boolean rootSeen = false;
        XMLStreamReader reader = null;
        try {
            reader = CapParser.secureInputFactory().createXMLStreamReader(
                    new ByteArrayInputStream(body == null ? new byte[0] : body));
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.DTD || event == XMLStreamConstants.ENTITY_REFERENCE) {
                    throw new UpstreamFormatException("PSK Reporter responses must not contain a DOCTYPE or entity"
                            + " references", "");
                }
                if (event != XMLStreamConstants.START_ELEMENT) {
                    continue;
                }
                String name = reader.getLocalName();
                if (!rootSeen) {
                    if (!"receptionReports".equals(name)) {
                        throw new UpstreamFormatException("Expected a receptionReports document, got <" + name + ">", "");
                    }
                    rootSeen = true;
                } else if ("activeReceiver".equals(name)) {
                    receivers++;
                } else if ("receptionReport".equals(name)) {
                    reports++;
                    if (byId.size() >= cap) {
                        capped = true;
                        continue;
                    }
                    String reason = report(reader, byId);
                    if (reason != null) {
                        skipped.merge(reason, 1, Integer::sum);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The PSK Reporter response is not well-formed XML: " + e.getMessage(), "",
                    e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // Nothing to release beyond the in-memory stream.
                }
            }
        }
        if (!rootSeen) {
            throw new UpstreamFormatException("The PSK Reporter response is empty", "");
        }
        return new Result(new ArrayList<>(byId.values()), reports, receivers, capped, skipped);
    }

    /** Maps one {@code receptionReport}; returns a skip reason, or null when it was kept. */
    private static String report(XMLStreamReader reader, Map<String, NormalizedRecord> byId) {
        String sender = attr(reader, "senderCallsign");
        String receiver = attr(reader, "receiverCallsign");
        String frequencyText = attr(reader, "frequency");
        String flowText = attr(reader, "flowStartSeconds");
        if (sender == null || receiver == null || frequencyText == null || flowText == null) {
            return MISSING_FIELDS;
        }
        long frequency;
        long flow;
        try {
            frequency = Long.parseLong(frequencyText);
            flow = Long.parseLong(flowText);
        } catch (NumberFormatException e) {
            return BAD_NUMBER;
        }
        if (frequency <= 0 || flow <= 0) {
            return BAD_NUMBER;
        }
        String id = sender + "|" + receiver + "|" + (frequency / 1000) + "|" + flow;
        if (byId.containsKey(id)) {
            return DUPLICATE;
        }
        Integer snr = integer(attr(reader, "sNR"));
        String senderLocator = attr(reader, "senderLocator");
        Instant at = Instant.ofEpochSecond(flow);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(at)
                .eventTime(at)
                .put("sender", sender)
                .put("senderLocator", senderLocator)
                .put("mode", attr(reader, "mode"))
                .put("frequencyHz", frequency)
                .put("band", band(frequency))
                .put("snr", snr)
                .put("dxcc", attr(reader, "senderDXCC"))
                .put("receiverLocator", attr(reader, "receiverLocator"))
                .observers(List.of(new Observer(receiver, frequency, null, snr == null ? null : snr.doubleValue(),
                        null)));
        Maidenhead.Cell cell = Maidenhead.decodeLenient(senderLocator);
        if (cell != null) {
            b.point(round(cell.centerLon(), 6), round(cell.centerLat(), 6));
            b.quality(new Quality(round(cell.halfDiagonalMeters(), 0), null, null, null));
        }
        byId.put(id, b.build());
        return null;
    }

    /** The amateur band of {@code hz} ({@code 20m}), or null outside every band. */
    public static String band(long hz) {
        for (Object[] band : BANDS) {
            if (hz >= (Long) band[1] && hz <= (Long) band[2]) {
                return (String) band[0];
            }
        }
        return null;
    }

    private static String attr(XMLStreamReader reader, String name) {
        String value = reader.getAttributeValue(null, name);
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static Integer integer(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}

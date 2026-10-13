package ai.foresignal.source.adapters.aisstream;

import ai.foresignal.source.spi.ValidatedParams;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * The aisstream subscription frame (prompt 063):
 * {@code {"APIKey":"…","BoundingBoxes":[[[minLat,minLon],[maxLat,maxLon]],…],"FilterMessageTypes":[…],
 * "FiltersShipMMSI":[…]}}.
 *
 * <p>Boxes are <strong>latitude first</strong>, one corner pair per box, converted from the
 * {@code minLon,minLat,maxLon,maxLat} of the {@code bbox} parameter. {@code FiltersShipMMSI} is left out
 * when the {@code mmsi} parameter is empty. {@link #toString()} never shows the key; only {@link #toJson()}
 * does, and that text goes only into the WebSocket frame.
 *
 * @param apiKey the account key
 * @param boxes each box as {@code {minLat, minLon, maxLat, maxLon}}
 * @param messageTypes the message types to receive
 * @param mmsi the MMSIs to filter on; empty means every vessel
 */
public record AisSubscription(String apiKey, List<double[]> boxes, List<String> messageTypes, List<String> mmsi) {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Copies the lists. */
    public AisSubscription {
        boxes = boxes.stream().map(double[]::clone).toList();
        messageTypes = List.copyOf(messageTypes);
        mmsi = List.copyOf(mmsi);
    }

    /**
     * The subscription for {@code apiKey} and the validated parameters.
     *
     * @throws IllegalArgumentException when a {@code bbox} entry is not four numbers (checkParams refuses
     *     those first)
     */
    public static AisSubscription of(String apiKey, ValidatedParams params) {
        List<String> bboxes = params.stringList(AisStreamAdapter.BBOX_PARAM);
        if (bboxes.isEmpty()) {
            bboxes = AisStreamAdapter.DEFAULT_BBOX;
        }
        List<double[]> boxes = new ArrayList<>();
        for (String entry : bboxes) {
            double[] b = AisStreamAdapter.parseBox(entry);
            if (b == null) {
                throw new IllegalArgumentException("bbox entry '" + entry + "' is not minLon,minLat,maxLon,maxLat");
            }
            // minLon,minLat,maxLon,maxLat -> {minLat, minLon, maxLat, maxLon}
            boxes.add(new double[] {b[1], b[0], b[3], b[2]});
        }
        List<String> types = params.stringList(AisStreamAdapter.TYPES_PARAM);
        if (types.isEmpty()) {
            types = AisStreamMessageParser.MERGED_TYPES;
        }
        return new AisSubscription(apiKey, boxes, types, params.stringList(AisStreamAdapter.MMSI_PARAM));
    }

    /** The frame text, with the key. Never log it. */
    public String toJson() {
        Map<String, Object> frame = new LinkedHashMap<>();
        frame.put("APIKey", apiKey);
        List<List<List<Double>>> boundingBoxes = new ArrayList<>();
        for (double[] b : boxes) {
            boundingBoxes.add(List.of(List.of(b[0], b[1]), List.of(b[2], b[3])));
        }
        frame.put("BoundingBoxes", boundingBoxes);
        frame.put("FilterMessageTypes", messageTypes);
        if (!mmsi.isEmpty()) {
            frame.put("FiltersShipMMSI", mmsi);
        }
        return JSON.writeValueAsString(frame);
    }

    /** True when both subscribe to the same thing with the same key. */
    public boolean sameAs(AisSubscription other) {
        if (other == null || !apiKey.equals(other.apiKey) || !messageTypes.equals(other.messageTypes)
                || !mmsi.equals(other.mmsi) || boxes.size() != other.boxes.size()) {
            return false;
        }
        for (int i = 0; i < boxes.size(); i++) {
            if (!java.util.Arrays.equals(boxes.get(i), other.boxes.get(i))) {
                return false;
            }
        }
        return true;
    }

    /** The subscription without the key, safe to log. */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("AisSubscription[APIKey=(redacted), boxes=[");
        for (int i = 0; i < boxes.size(); i++) {
            double[] b = boxes.get(i);
            text.append(i == 0 ? "" : ", ").append("[[").append(b[0]).append(',').append(b[1]).append("],[")
                    .append(b[2]).append(',').append(b[3]).append("]]");
        }
        return text.append("], messageTypes=").append(messageTypes).append(", mmsi=").append(mmsi).append(']')
                .toString();
    }
}

package ai.foresignal.source.adapters.cctv;

/**
 * One camera as a pack parser read it, before {@link CameraRecordMapper} checks it (prompt 072).
 *
 * <p>Parsers fill in what the upstream publishes and nothing else: no synthetic heading, no pose, no fallback
 * coordinates. Coordinates may be null (the mapper skips and counts the camera); URLs are the raw published text
 * (the mapper drops any that is not on the pack's allowed origins).
 *
 * @param id the upstream camera id, unique within the source ({@code stationId:presetId} for Digitraffic)
 * @param lat latitude in degrees, or null when the upstream gave none
 * @param lon longitude in degrees, or null when the upstream gave none
 * @param name the camera's display name
 * @param provider the operator or credit to show, or null for the pack's provider
 * @param region the published region, district or city, or null
 * @param road the published road, or null
 * @param direction the published direction text, verbatim, or null
 * @param headingDeg the facing in degrees, only when the upstream publishes a direction for the camera; else null
 * @param active false when the upstream marks the camera out of service
 * @param imageUrl the published still-image URL, or null
 * @param videoUrl the published official live-stream (HLS) URL, or null
 * @param refreshSeconds the published still refresh interval in seconds, or null
 */
public record CameraEntry(
        String id,
        Double lat,
        Double lon,
        String name,
        String provider,
        String region,
        String road,
        String direction,
        Double headingDeg,
        boolean active,
        String imageUrl,
        String videoUrl,
        Integer refreshSeconds) {

    /**
     * Starts an entry with only an id and a position.
     *
     * @param id the upstream camera id
     * @param lat the latitude, or null
     * @param lon the longitude, or null
     */
    public static Builder builder(String id, Double lat, Double lon) {
        return new Builder(id, lat, lon);
    }

    /** Fluent construction, so a parser sets only what its upstream publishes. */
    public static final class Builder {

        private final String id;
        private final Double lat;
        private final Double lon;
        private String name;
        private String provider;
        private String region;
        private String road;
        private String direction;
        private Double headingDeg;
        private boolean active = true;
        private String imageUrl;
        private String videoUrl;
        private Integer refreshSeconds;

        private Builder(String id, Double lat, Double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }

        /** Sets the display name. */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /** Sets the operator or credit shown instead of the pack's provider. */
        public Builder provider(String value) {
            this.provider = value;
            return this;
        }

        /** Sets the published region. */
        public Builder region(String value) {
            this.region = value;
            return this;
        }

        /** Sets the published road. */
        public Builder road(String value) {
            this.road = value;
            return this;
        }

        /** Sets the published direction text. */
        public Builder direction(String value) {
            this.direction = value;
            return this;
        }

        /** Sets the published facing in degrees. */
        public Builder headingDeg(Double value) {
            this.headingDeg = value;
            return this;
        }

        /** Sets whether the camera is in service. */
        public Builder active(boolean value) {
            this.active = value;
            return this;
        }

        /** Sets the published still-image URL. */
        public Builder imageUrl(String value) {
            this.imageUrl = value;
            return this;
        }

        /** Sets the published live-stream URL. */
        public Builder videoUrl(String value) {
            this.videoUrl = value;
            return this;
        }

        /** Sets the published refresh interval in seconds. */
        public Builder refreshSeconds(Integer value) {
            this.refreshSeconds = value;
            return this;
        }

        /** The entry. */
        public CameraEntry build() {
            return new CameraEntry(id, lat, lon, name, provider, region, road, direction, headingDeg, active,
                    imageUrl, videoUrl, refreshSeconds);
        }
    }
}

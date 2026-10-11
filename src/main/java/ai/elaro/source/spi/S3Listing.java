package ai.elaro.source.spi;

import java.util.List;

/**
 * One page of a public S3 bucket listing.
 *
 * @param objects the objects on this page, in key order
 * @param truncated true when more pages follow
 * @param nextStartAfter the key to pass as {@code startAfter} for the next page, or null
 */
public record S3Listing(List<S3Object> objects, boolean truncated, String nextStartAfter) {

    public S3Listing {
        objects = objects == null ? List.of() : List.copyOf(objects);
    }

    public static S3Listing empty() {
        return new S3Listing(List.of(), false, null);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }
}

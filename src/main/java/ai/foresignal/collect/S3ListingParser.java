package ai.foresignal.collect;

import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.StringReader;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Parses an S3 {@code ListObjectsV2} response.
 *
 * <p><strong>External entities are disabled.</strong> The XML comes from a remote host, and an XML
 * parser with entity resolution left on is a file-read and SSRF primitive: a response containing
 * {@code <!ENTITY x SYSTEM "file:///data/secret.key">} would otherwise be expanded by us. StAX with
 * {@code SUPPORT_DTD=false} and {@code IS_SUPPORTING_EXTERNAL_ENTITIES=false} cannot be talked into
 * it.
 *
 * <p>Streaming rather than DOM, because a listing page can hold a thousand keys and there is no
 * reason to build a tree for them.
 */
final class S3ListingParser {

    private S3ListingParser() {}

    /** The hardened factory. Created per parse: {@link XMLInputFactory} is not thread-safe. */
    private static XMLInputFactory hardenedFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        return factory;
    }

    /**
     * Parses the listing.
     *
     * @throws UpstreamFormatException when the XML cannot be read
     */
    static S3Listing parse(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new UpstreamFormatException("The S3 listing response was empty", xml);
        }
        List<S3Object> objects = new ArrayList<>();
        boolean truncated = false;
        String nextContinuation = null;

        String key = null;
        Long size = null;
        String etag = null;
        Instant lastModified = null;
        boolean inContents = false;

        try {
            XMLStreamReader reader = hardenedFactory().createXMLStreamReader(new StringReader(xml));
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    switch (name) {
                        case "Contents" -> {
                            inContents = true;
                            key = null;
                            size = null;
                            etag = null;
                            lastModified = null;
                        }
                        case "Key" -> {
                            if (inContents) {
                                key = reader.getElementText();
                            }
                        }
                        case "Size" -> {
                            if (inContents) {
                                size = parseLong(reader.getElementText());
                            }
                        }
                        case "ETag" -> {
                            if (inContents) {
                                etag = stripQuotes(reader.getElementText());
                            }
                        }
                        case "LastModified" -> {
                            if (inContents) {
                                lastModified = parseInstant(reader.getElementText());
                            }
                        }
                        case "IsTruncated" -> truncated = Boolean.parseBoolean(reader.getElementText().trim());
                        case "NextContinuationToken" -> nextContinuation = reader.getElementText();
                        default -> {
                            // Other elements carry no information this parser needs.
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT
                        && "Contents".equals(reader.getLocalName())) {
                    inContents = false;
                    if (key != null) {
                        objects.add(new S3Object(key, size == null ? 0 : size, etag, lastModified));
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException(
                    "Could not parse the S3 listing: " + e.getMessage(), xml, e);
        }

        // Paging uses the last key rather than the continuation token, because listS3 takes
        // start-after: the two are interchangeable for a plain prefix listing, and start-after
        // needs no state to be carried between calls.
        String nextStartAfter =
                truncated && !objects.isEmpty()
                        ? objects.get(objects.size() - 1).key()
                        : nextContinuation;
        return new S3Listing(objects, truncated, truncated ? nextStartAfter : null);
    }

    private static Long parseLong(String text) {
        try {
            return Long.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant parseInstant(String text) {
        try {
            return Instant.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String stripQuotes(String text) {
        String trimmed = text.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}

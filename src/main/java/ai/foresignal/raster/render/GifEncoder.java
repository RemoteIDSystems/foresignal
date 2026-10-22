package ai.foresignal.raster.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Node;

/**
 * Animated GIF loops (prompt 108; Weather-Imagery-Design §11): the JDK ImageIO GIF writer with
 * animation metadata. The palette comes straight from the frame images (a PNG-8 palette maps to a
 * GIF palette), so a categorical style keeps its exact colors. Frames render through the render
 * queue at prefetch priority on the way in; this class is pure encoding.
 */
public final class GifEncoder {

    /** The fastest frame delay, ms. */
    public static final int MIN_DELAY_MS = 300;

    /** The slowest frame delay, ms. */
    public static final int MAX_DELAY_MS = 1000;

    /** The delay when the request does not say, ms. */
    public static final int DEFAULT_DELAY_MS = 500;

    /** The widest GIF, pixels. */
    public static final int MAX_WIDTH = 800;

    private GifEncoder() {}

    /** Encodes {@code frames} as a looping GIF with {@code delayMs} between frames. */
    public static byte[] encode(List<BufferedImage> frames, int delayMs) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("A GIF loop needs at least one frame.");
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No GIF writer is available");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            IIOMetadata first = null;
            for (int k = 0; k < frames.size(); k++) {
                BufferedImage frame = toGifImage(frames.get(k));
                IIOMetadata metadata = writer.getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromRenderedImage(frame), writer.getDefaultWriteParam());
                setFrameMetadata(metadata, delayMs, k == 0);
                writer.writeToSequence(new IIOImage(frame, null, metadata), writer.getDefaultWriteParam());
                if (k == 0) {
                    first = metadata;
                }
            }
            writer.endWriteSequence();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode the GIF loop", e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    /** Converts any image to an indexed image the GIF writer likes. */
    private static BufferedImage toGifImage(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            return source;
        }
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        java.awt.Graphics2D g = out.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    /** Sets the GraphicControlExtension delay and, on the first frame, the loop extension. */
    private static void setFrameMetadata(IIOMetadata metadata, int delayMs, boolean first) {
        String format = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
        IIOMetadataNode gce = child(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "FALSE");
        gce.setAttribute("delayTime", Integer.toString(delayMs / 10));
        gce.setAttribute("transparentColorIndex", "0");
        if (first) {
            IIOMetadataNode app = child(root, "ApplicationExtensions");
            IIOMetadataNode ext = new IIOMetadataNode("ApplicationExtension");
            ext.setAttribute("applicationID", "NETSCAPE");
            ext.setAttribute("authenticationCode", "2.0");
            ext.setUserObject(new byte[] {0x1, 0x0, 0x0});
            app.appendChild(ext);
        }
        try {
            metadata.setFromTree(format, root);
        } catch (Exception e) {
            throw new IllegalStateException("Could not set GIF metadata", e);
        }
    }

    private static IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) n;
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        root.appendChild(node);
        return node;
    }
}

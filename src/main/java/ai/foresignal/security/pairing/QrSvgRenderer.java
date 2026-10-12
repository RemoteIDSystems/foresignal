package ai.foresignal.security.pairing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Renders a QR code as SVG with ZXing core (Apache-2.0) for {@code getPairingCodeQr} (prompt 023).
 *
 * <p>One SVG unit is one module; the image has a four-module quiet zone, error correction level M,
 * and draws every dark module as one {@code Mx,yh1v1h-1z} subpath of a single {@code <path>}, so it
 * scales without blurring and contains nothing active (no script, no external reference).
 */
@Component
public class QrSvgRenderer {

    /** Modules of quiet zone around the symbol. */
    static final int QUIET_ZONE = 4;

    /**
     * The SVG document encoding {@code payload}.
     *
     * @throws IllegalArgumentException when the payload cannot be encoded
     */
    public String render(String payload) {
        BitMatrix matrix;
        try {
            matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 0, 0, Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, QUIET_ZONE));
        } catch (WriterException e) {
            throw new IllegalArgumentException("The pairing payload cannot be encoded as a QR code", e);
        }
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        StringBuilder path = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    path.append('M').append(x).append(',').append(y).append("h1v1h-1z");
                }
            }
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + width + " " + height + "\" width=\""
                + (width * 8) + "\" height=\"" + (height * 8) + "\" shape-rendering=\"crispEdges\">"
                + "<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>"
                + "<path fill=\"#000000\" d=\"" + path + "\"/></svg>\n";
    }
}

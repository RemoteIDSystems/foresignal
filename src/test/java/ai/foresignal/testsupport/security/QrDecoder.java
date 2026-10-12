package ai.foresignal.testsupport.security;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decodes the SVG QR codes {@code QrSvgRenderer} draws (prompt 023) with ZXing's reader: the
 * {@code Mx,yh1v1h-1z} modules are rasterized and decoded, so a test proves what a phone camera
 * would read.
 */
public final class QrDecoder {

    private static final Pattern VIEW_BOX = Pattern.compile("viewBox=\"0 0 (\\d+) (\\d+)\"");
    private static final Pattern MODULE = Pattern.compile("M(\\d+),(\\d+)h1v1h-1z");
    private static final int SCALE = 4;

    private QrDecoder() {}

    /** The text encoded in {@code svg}. */
    public static String decode(String svg) throws Exception {
        Matcher box = VIEW_BOX.matcher(svg);
        if (!box.find()) {
            throw new IllegalArgumentException("No viewBox in the SVG");
        }
        int width = Integer.parseInt(box.group(1));
        int height = Integer.parseInt(box.group(2));
        int[] pixels = new int[width * SCALE * height * SCALE];
        java.util.Arrays.fill(pixels, 0xFFFFFFFF);
        Matcher module = MODULE.matcher(svg);
        while (module.find()) {
            int x = Integer.parseInt(module.group(1));
            int y = Integer.parseInt(module.group(2));
            for (int dy = 0; dy < SCALE; dy++) {
                for (int dx = 0; dx < SCALE; dx++) {
                    pixels[(y * SCALE + dy) * width * SCALE + x * SCALE + dx] = 0xFF000000;
                }
            }
        }
        RGBLuminanceSource source = new RGBLuminanceSource(width * SCALE, height * SCALE, pixels);
        return new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(source)),
                Map.of(DecodeHintType.PURE_BARCODE, Boolean.TRUE)).getText();
    }
}

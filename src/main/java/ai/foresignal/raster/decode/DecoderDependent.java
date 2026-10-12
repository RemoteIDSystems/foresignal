package ai.foresignal.raster.decode;

import java.util.Map;
import java.util.Set;

/**
 * Implemented by a product source whose products need particular decoding paths (prompt 046), so
 * the {@link DecoderSelfTest} can disable exactly the products a failed path affects.
 */
public interface DecoderDependent {

    /**
     * The decoding paths each product needs.
     *
     * @return product id to the capabilities its frames are decoded with
     */
    Map<String, Set<DecoderCapability>> decoderRequirements();
}

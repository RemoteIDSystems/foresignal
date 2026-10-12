package ai.foresignal.raster;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import org.springframework.stereotype.Component;

/**
 * The DERIVED source that owns the external catalog's products (prompt 045): it makes no upstream
 * requests at all (a DERIVED source gets no HTTP client) and writes no records; it exists so the
 * {@link ExternalCatalog} entries are ordinary {@code kind=external} products in the catalog.
 *
 * <p>Lives in {@code raster}, not {@code source.adapters}: it carries no analytic data, so it has no
 * retention class (Conventions "Analytic retention").
 */
@Component
public class ExternalCatalogSource implements SourceAdapter {

    /** The source id. */
    public static final String SOURCE_ID = "external-catalogue";

    private final SourceDescriptor descriptor;

    /** Creates the source over the shipped catalog. */
    public ExternalCatalogSource(ExternalCatalog catalog) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(SOURCE_ID)
                .name("External catalog")
                .description("Client-direct GIBS and nowCOAST map layers; Foresignal lists them and never fetches them")
                .attribution("NASA EOSDIS GIBS; NOAA/NWS nowCOAST")
                .licence("US-PD")
                .mode(Mode.DERIVED)
                .kind(ProductSpec.RECORD_KIND)
                .defaultBudget(Budget.of(1, 1));
        catalog.specs().forEach(b::product);
        this.descriptor = b.build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }
}

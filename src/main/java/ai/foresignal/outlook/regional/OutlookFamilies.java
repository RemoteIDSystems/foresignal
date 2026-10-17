package ai.foresignal.outlook.regional;

import ai.foresignal.baserate.reference.ReferenceFamilies;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The regional outlook families of {@code intel/outlook-families.json} (prompt 126): {@code conflict:battles},
 * {@code conflict:explosions-remote}, {@code conflict:violence-civilians}, {@code conflict:protests}, {@code outage},
 * {@code weather-severe} and {@code seismic:m4.5} by default, each mapped to the reference family its counts come
 * from, a ledger category and a 116 observability family.
 */
@Component
public class OutlookFamilies {

    /** The bundled resource. */
    public static final String RESOURCE = "intel/outlook-families.json";

    /**
     * One family.
     *
     * @param id the family id
     * @param label the label
     * @param category the ledger category of its questions
     * @param referenceFamily the {@code reference_history} family its counts come from
     * @param observabilityFamily the 116 observability family
     * @param sources the data sources, in words
     */
    public record Family(String id, String label, String category, String referenceFamily,
            String observabilityFamily, String sources) {}

    private final List<Family> families;

    /** Loads the bundled families. */
    public OutlookFamilies(JsonMapper json) {
        this.families = load(json);
    }

    /** Families from an explicit list (tests). */
    public OutlookFamilies(List<Family> families) {
        this.families = List.copyOf(families);
    }

    /** Every family, in file order. */
    public List<Family> all() {
        return families;
    }

    /** The family with {@code id}. */
    public Optional<Family> find(String id) {
        return families.stream().filter(f -> f.id().equals(id)).findFirst();
    }

    static List<Family> load(JsonMapper json) {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            JsonNode root = json.readTree(in);
            List<Family> out = new ArrayList<>();
            for (JsonNode f : root.get("families")) {
                String id = f.get("id").asString();
                String reference = f.get("referenceFamily").asString();
                if (!ReferenceFamilies.FAMILY.matcher(id).matches()
                        || !ReferenceFamilies.FAMILY.matcher(reference).matches()) {
                    throw new IllegalStateException(RESOURCE + ": invalid family " + id);
                }
                out.add(new Family(id, f.path("label").asString(id), f.path("category").asString("activity"),
                        reference, f.path("observabilityFamily").asString(ReferenceFamilies.root(reference)),
                        f.path("sources").asString("")));
            }
            return List.copyOf(out);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }
}

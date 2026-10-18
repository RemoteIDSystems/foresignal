package ai.foresignal.doctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link UnifiedDiffApplier} and the patch validation of {@link DoctorLlm} (prompt 140, task 5): a clean apply, a
 * context mismatch, multiple hunks, and a file outside the adapter source catalog refused. Everything happens in
 * memory.
 */
class UnifiedDiffApplierTest {

    static final String SOURCE = String.join("\n",
            "package ai.foresignal.source.adapters.usgs;",
            "",
            "final class UsgsGeoJsonParser {",
            "",
            "    static double magnitude(JsonNode properties) {",
            "        JsonNode mag = properties.get(\"mag\");",
            "        return mag == null ? Double.NaN : mag.asDouble();",
            "    }",
            "",
            "    static String place(JsonNode properties) {",
            "        return properties.path(\"place\").asString();",
            "    }",
            "}",
            "");

    @Test
    @DisplayName("a clean single-hunk patch applies and passes the sanity checks")
    void cleanApply() {
        String diff = String.join("\n",
                "--- a/usgs/UsgsGeoJsonParser.java",
                "+++ b/usgs/UsgsGeoJsonParser.java",
                "@@ -5,4 +5,4 @@",
                "     static double magnitude(JsonNode properties) {",
                "-        JsonNode mag = properties.get(\"mag\");",
                "+        JsonNode mag = properties.get(\"magnitude\");",
                "         return mag == null ? Double.NaN : mag.asDouble();",
                "     }");

        UnifiedDiffApplier.Result result = UnifiedDiffApplier.apply(SOURCE, diff);

        assertThat(result.applies()).isTrue();
        assertThat(result.hunks()).isEqualTo(1);
        assertThat(result.patched()).contains("properties.get(\"magnitude\")").doesNotContain("get(\"mag\")");
        assertThat(result.patched()).endsWith("}\n");
        PatchSanity.Checks checks = PatchSanity.check(SOURCE, result.patched());
        assertThat(checks.bracesBalanced()).isTrue();
        assertThat(checks.classNameUnchanged()).isTrue();
    }

    @Test
    @DisplayName("a hunk whose context does not match is refused with the line it was looking at")
    void contextMismatch() {
        String diff = String.join("\n",
                "@@ -5,3 +5,3 @@",
                "     static double magnitude(JsonNode props) {",
                "-        JsonNode mag = properties.get(\"mag\");",
                "+        JsonNode mag = properties.get(\"magnitude\");",
                "         return mag == null ? Double.NaN : mag.asDouble();");

        UnifiedDiffApplier.Result result = UnifiedDiffApplier.apply(SOURCE, diff);

        assertThat(result.applies()).isFalse();
        assertThat(result.patched()).isNull();
        assertThat(result.error()).contains("hunk 1").contains("line 5");
    }

    @Test
    @DisplayName("multiple hunks apply in order, even when the stated lines are a little off")
    void multipleHunks() {
        String diff = String.join("\n",
                "@@ -6,1 +6,1 @@",
                "-        JsonNode mag = properties.get(\"mag\");",
                "+        JsonNode mag = properties.get(\"magnitude\");",
                "@@ -13,1 +13,1 @@",
                "-        return properties.path(\"place\").asString();",
                "+        return properties.path(\"location\").asString();");

        UnifiedDiffApplier.Result result = UnifiedDiffApplier.apply(SOURCE, diff);

        assertThat(result.applies()).isTrue();
        assertThat(result.hunks()).isEqualTo(2);
        assertThat(result.patched()).contains("\"magnitude\"").contains("\"location\"");
    }

    @Test
    @DisplayName("a patch for two files, an incomplete hunk and an empty patch are refused")
    void malformed() {
        assertThat(UnifiedDiffApplier.apply(SOURCE, "--- a/x\n+++ b/x\n--- a/y\n+++ b/y\n@@ -1 +1 @@\n-a\n+b").error())
                .contains("more than one file");
        assertThat(UnifiedDiffApplier.apply(SOURCE, "@@ -6,2 +6,2 @@\n-        JsonNode mag = properties.get(\"mag\");")
                .applies()).isFalse();
        assertThat(UnifiedDiffApplier.apply(SOURCE, "").applies()).isFalse();
        assertThat(UnifiedDiffApplier.apply(SOURCE, "no hunk here").error()).contains("no hunk");
    }

    @Test
    @DisplayName("sanity checks flag unbalanced braces and a renamed class, ignoring braces in strings and comments")
    void sanity() {
        assertThat(PatchSanity.balanced("class A { String s = \"}\"; // }\n char c = '{'; }")).isTrue();
        assertThat(PatchSanity.balanced("class A { void m() { }")).isFalse();
        assertThat(PatchSanity.check(SOURCE, SOURCE.replace("final class UsgsGeoJsonParser", "final class Other"))
                .classNameUnchanged()).isFalse();
        assertThat(PatchSanity.typeName("public record Point(double x) {}")).isEqualTo("Point");
    }

    @Test
    @DisplayName("a patch for a file outside the adapter source catalog is refused; a catalog file is validated")
    @SuppressWarnings("unchecked")
    void fileNotInCatalog() {
        AdapterSourceCatalog catalog = new AdapterSourceCatalog(AdapterSourceCatalog.ROOT, JsonMapper.builder().build(),
                id -> Optional.of("usgs"));
        DoctorLlm llm = new DoctorLlm(org.mockito.Mockito.mock(ObjectProvider.class),
                org.mockito.Mockito.mock(ObjectProvider.class), catalog, JsonMapper.builder().build());

        var refused = llm.validatePatch("src/main/java/ai/foresignal/config/SecurityConfig.java",
                "@@ -1 +1 @@\n-a\n+b");
        assertThat(refused.get("patchApplies")).isEqualTo(false);
        assertThat(refused.get("error")).asString().contains("not in the adapter source catalog");
        assertThat(refused.get("label")).isEqualTo(DoctorLlm.PATCH_LABEL);

        var escape = llm.validatePatch("../../etc/passwd", "@@ -1 +1 @@\n-a\n+b");
        assertThat(escape.get("patchApplies")).isEqualTo(false);

        var known = llm.validatePatch("usgs/UsgsGeoJsonParser.java", "@@ -1,1 +1,1 @@\n-this line is not there\n+x");
        assertThat(known.get("file")).isEqualTo("usgs/UsgsGeoJsonParser.java");
        assertThat(known.get("patchApplies")).isEqualTo(false);
        assertThat(known.get("error")).asString().contains("does not apply");
        assertThat(AdapterSourceCatalog.normalize("b/src/main/java/ai/foresignal/source/adapters/usgs/A.java"))
                .isEqualTo("usgs/A.java");
    }
}

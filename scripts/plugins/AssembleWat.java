import com.dylibso.chicory.wabt.Wat2Wasm;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Assembles a WebAssembly text file into a Foresignal plugin directory (prompt 141): {@code plugin.wasm} plus a
 * {@code plugin.json} manifest carrying the binary's real SHA-256.
 *
 * <p>Used for the test plugins under {@code src/test/resources/plugins/} because the build machine has no Rust
 * toolchain. It runs outside Gradle as a single-file Java program with Chicory's {@code wabt} module (wabt's
 * {@code wat2wasm} compiled to JVM bytecode) on the class path; see {@code scripts/plugins/README.md}.
 *
 * <p>Usage: {@code java -cp <chicory jars> scripts/plugins/AssembleWat.java <in.wat> <out-dir> <name> <version>}
 */
public final class AssembleWat {

    private AssembleWat() {}

    /**
     * Assembles one module.
     *
     * @param args the WAT file, the output directory, the plugin name and its version
     * @throws Exception when the file cannot be read, assembled or written
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("usage: AssembleWat <in.wat> <out-dir> <name> <version>");
            System.exit(1);
        }
        Path wat = Path.of(args[0]);
        Path out = Path.of(args[1]);
        String name = args[2];
        String version = args[3];
        byte[] wasm = Wat2Wasm.parse(wat.toFile());
        Files.createDirectories(out);
        Files.write(out.resolve("plugin.wasm"), wasm);
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(wasm));
        String manifest = "{\n"
                + "  \"name\": \"" + name + "\",\n"
                + "  \"version\": \"" + version + "\",\n"
                + "  \"abi\": 1,\n"
                + "  \"kind\": \"parser\",\n"
                + "  \"sha256\": \"" + sha + "\"\n"
                + "}\n";
        Files.writeString(out.resolve("plugin.json"), manifest, StandardCharsets.UTF_8);
        System.out.println(name + " " + wasm.length + " bytes sha256=" + sha);
    }
}

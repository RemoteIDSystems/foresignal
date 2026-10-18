package ai.foresignal.plugin;

import com.dylibso.chicory.runtime.ByteArrayMemory;
import com.dylibso.chicory.runtime.ChicoryInterruptedException;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.wasm.ChicoryException;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.Export;
import com.dylibso.chicory.wasm.types.ExportSection;
import com.dylibso.chicory.wasm.types.ExternalType;
import com.dylibso.chicory.wasm.types.FunctionImport;
import com.dylibso.chicory.wasm.types.FunctionType;
import com.dylibso.chicory.wasm.types.Import;
import com.dylibso.chicory.wasm.types.ImportSection;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import com.dylibso.chicory.wasm.types.MemorySection;
import com.dylibso.chicory.wasm.types.ValType;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs one ABI v1 parser module in the Chicory interpreter (prompt 141, {@code docs/Plugin-Design.md} §3–§4).
 *
 * <p><strong>Load</strong> ({@link #load}) parses and checks the module once:
 * <ul>
 *   <li>imports: only the function {@code env.foresignal_log(i32, i32, i32)}; any other import (every WASI function,
 *       any memory, table, global or tag) refuses the module, so a plugin has no clock, filesystem or network;
 *   <li>exports: {@code memory}, {@code alloc(i32) -> i32}, {@code dealloc(i32, i32)} and
 *       {@code foresignal_parse(i32, i32) -> i32};
 *   <li>one unshared memory whose initial size is within the cap.
 * </ul>
 *
 * <p><strong>Call</strong> ({@link #parse}): every call gets a fresh instance, so no state survives between calls. On a
 * worker thread the host instantiates the module with its memory maximum clamped to {@link PluginLimits#maxMemoryPages()}
 * (growth beyond fails, which the module sees as {@code memory.grow} returning -1), calls {@code alloc(len)}, copies the
 * input, calls {@code foresignal_parse(ptr, len)}, reads the returned length-prefixed buffer (a little-endian u32 byte
 * count, then the bytes) and calls {@code dealloc} for both buffers. The caller waits at most
 * {@link PluginLimits#timeout()}; then it interrupts the worker, which Chicory's interpreter honors on the next function
 * call or loop branch by throwing {@link ChicoryInterruptedException}.
 */
public final class WasmParserHost {

    /** The module of the single host import. */
    public static final String IMPORT_MODULE = "env";

    /** The single host import. */
    public static final String LOG_IMPORT = "foresignal_log";

    /** The parse export. */
    public static final String PARSE_EXPORT = "foresignal_parse";

    /** The allocation export. */
    public static final String ALLOC_EXPORT = "alloc";

    /** The deallocation export. */
    public static final String DEALLOC_EXPORT = "dealloc";

    /** The memory export. */
    public static final String MEMORY_EXPORT = "memory";

    /** How long a timed-out worker is given to stop after the interrupt. */
    static final long STOP_GRACE_MILLIS = 2_000;

    /** The worker's stack: deep but bounded recursion in the interpreter. */
    static final long WORKER_STACK_BYTES = 16L * 1024 * 1024;

    private static final FunctionType LOG_TYPE = FunctionType.of(List.of(ValType.I32, ValType.I32, ValType.I32), List.of());
    private static final FunctionType ALLOC_TYPE = FunctionType.of(List.of(ValType.I32), List.of(ValType.I32));
    private static final FunctionType DEALLOC_TYPE = FunctionType.of(List.of(ValType.I32, ValType.I32), List.of());
    private static final FunctionType PARSE_TYPE =
            FunctionType.of(List.of(ValType.I32, ValType.I32), List.of(ValType.I32));

    private final String name;
    private final WasmModule module;
    private final PluginLimits limits;
    private final PluginLog log;
    private final boolean importsLog;

    private WasmParserHost(String name, WasmModule module, PluginLimits limits, PluginLog log, boolean importsLog) {
        this.name = name;
        this.module = module;
        this.limits = limits;
        this.log = log;
        this.importsLog = importsLog;
    }

    /**
     * Loads a module with the default clock and the logger sink.
     *
     * @param name the plugin name
     * @param wasm the module bytes
     * @param limits the limits
     * @throws PluginException ({@link PluginException.Reason#INVALID_MODULE}) when the module is refused
     */
    public static WasmParserHost load(String name, byte[] wasm, PluginLimits limits) {
        return load(name, wasm, limits, Clock.systemUTC(), null);
    }

    /**
     * Loads and checks a module (see the class note).
     *
     * @param name the plugin name
     * @param wasm the module bytes
     * @param limits the limits
     * @param clock the clock of the log rate limit
     * @param sink where plugin log messages go, or null for the logger {@code foresignal.plugin.<name>}
     * @throws PluginException ({@link PluginException.Reason#INVALID_MODULE}) when the module is refused
     */
    public static WasmParserHost load(String name, byte[] wasm, PluginLimits limits, Clock clock, PluginLog.Sink sink) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(limits);
        if (wasm == null || wasm.length == 0) {
            throw invalid(name, "plugin.wasm is empty");
        }
        WasmModule module;
        try {
            module = Parser.parse(wasm);
        } catch (ChicoryException | IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            throw new PluginException(name, PluginException.Reason.INVALID_MODULE,
                    "plugin.wasm is not a valid WebAssembly module: " + e.getMessage(), e);
        }
        boolean importsLog = checkImports(name, module);
        checkMemory(name, module, limits);
        checkExports(name, module);
        return new WasmParserHost(name, module, limits, new PluginLog(name, limits, clock, sink), importsLog);
    }

    /** The plugin name. */
    public String name() {
        return name;
    }

    /** The limits this host enforces. */
    public PluginLimits limits() {
        return limits;
    }

    /** True when the module imports {@code env.foresignal_log}. */
    public boolean importsLog() {
        return importsLog;
    }

    /**
     * Runs {@code foresignal_parse} once on {@code input} (see the class note).
     *
     * @param input the raw upstream bytes
     * @return the plugin's output bytes (UTF-8 JSON, not yet checked)
     * @throws PluginException when the input is too large, or the call times out, traps, runs out of memory or returns
     *     no, too much or out-of-bounds output
     */
    public byte[] parse(byte[] input) {
        byte[] in = input == null ? new byte[0] : input;
        if (in.length > limits.maxInputBytes()) {
            throw new PluginException(name, PluginException.Reason.INPUT_TOO_LARGE,
                    "the input is " + in.length + " bytes, over the " + limits.maxInputBytes() + "-byte limit");
        }
        AtomicReference<Object> outcome = new AtomicReference<>();
        PluginLog.Call call = log.startCall();
        Thread worker = Thread.ofPlatform().daemon().name("foresignal-plugin-" + name).stackSize(WORKER_STACK_BYTES)
                .unstarted(() -> {
                    try {
                        outcome.set(invoke(in, call));
                    } catch (Throwable t) {
                        outcome.set(t);
                    }
                });
        worker.start();
        try {
            worker.join(limits.timeout().toMillis());
            if (worker.isAlive()) {
                worker.interrupt();
                worker.join(STOP_GRACE_MILLIS);
                throw new PluginException(name, PluginException.Reason.TIMEOUT,
                        "the call ran longer than " + limits.timeout().toMillis() + " ms and was interrupted");
            }
        } catch (InterruptedException e) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new PluginException(name, PluginException.Reason.TIMEOUT, "the caller was interrupted", e);
        } finally {
            call.finish();
        }
        Object result = outcome.get();
        if (result instanceof byte[] bytes) {
            return bytes;
        }
        throw translate((Throwable) result);
    }

    private byte[] invoke(byte[] input, PluginLog.Call call) {
        Instance instance = Instance.builder(module)
                .withImportValues(imports(call))
                .withMemoryFactory(declared -> new ByteArrayMemory(capped(declared)))
                .build();
        ExportFunction alloc = instance.export(ALLOC_EXPORT);
        ExportFunction parse = instance.export(PARSE_EXPORT);
        ExportFunction dealloc = instance.export(DEALLOC_EXPORT);
        Memory memory = instance.memory();

        long inPtr = Integer.toUnsignedLong((int) alloc.apply(input.length)[0]);
        if (inPtr == 0 || inPtr + input.length > memoryBytes(memory)) {
            throw new PluginException(name, PluginException.Reason.OUT_OF_MEMORY,
                    "alloc(" + input.length + ") returned no usable buffer");
        }
        memory.write((int) inPtr, input);
        long outPtr = Integer.toUnsignedLong((int) parse.apply(inPtr, input.length)[0]);
        if (outPtr == 0) {
            throw new PluginException(name, PluginException.Reason.NO_OUTPUT, PARSE_EXPORT + " returned no output");
        }
        long size = memoryBytes(memory);
        if (outPtr + 4 > size) {
            throw new PluginException(name, PluginException.Reason.OUTPUT_OUT_OF_BOUNDS,
                    "the output pointer " + outPtr + " lies outside memory (" + size + " bytes)");
        }
        long length = memory.readU32((int) outPtr);
        if (length > limits.maxOutputBytes()) {
            throw new PluginException(name, PluginException.Reason.OUTPUT_TOO_LARGE,
                    "the output is " + length + " bytes, over the " + limits.maxOutputBytes() + "-byte limit");
        }
        if (outPtr + 4 + length > size) {
            throw new PluginException(name, PluginException.Reason.OUTPUT_OUT_OF_BOUNDS,
                    "the output of " + length + " bytes at " + outPtr + " runs past the end of memory");
        }
        byte[] output = memory.readBytes((int) outPtr + 4, (int) length);
        dealloc.apply(outPtr, length + 4);
        dealloc.apply(inPtr, input.length);
        return output;
    }

    private ImportValues imports(PluginLog.Call call) {
        if (!importsLog) {
            return ImportValues.empty();
        }
        HostFunction logFunction = new HostFunction(IMPORT_MODULE, LOG_IMPORT, LOG_TYPE, (instance, args) -> {
            int ptr = (int) args[1];
            int len = (int) args[2];
            byte[] bytes = null;
            if (len >= 0) {
                try {
                    bytes = instance.memory().readBytes(ptr, Math.min(len, limits.maxLogMessageBytes()));
                } catch (RuntimeException e) {
                    bytes = null;
                }
            }
            call.log((int) args[0], bytes);
            return null;
        });
        return ImportValues.builder().addFunction(logFunction).build();
    }

    private MemoryLimits capped(MemoryLimits declared) {
        int max = Math.min(declared.maximumPages(), limits.maxMemoryPages());
        return new MemoryLimits(Math.min(declared.initialPages(), max), max);
    }

    private static long memoryBytes(Memory memory) {
        return (long) memory.pages() * PluginLimits.PAGE_BYTES;
    }

    private PluginException translate(Throwable t) {
        if (t instanceof PluginException p) {
            return p;
        }
        if (t instanceof ChicoryInterruptedException) {
            return new PluginException(name, PluginException.Reason.TIMEOUT,
                    "the call ran longer than " + limits.timeout().toMillis() + " ms and was interrupted", t);
        }
        if (t instanceof StackOverflowError) {
            return new PluginException(name, PluginException.Reason.TRAP, "the call exhausted the stack", t);
        }
        if (t instanceof OutOfMemoryError) {
            return new PluginException(name, PluginException.Reason.OUT_OF_MEMORY,
                    "the host ran out of heap while running the plugin", t);
        }
        if (t instanceof ChicoryException || t instanceof RuntimeException) {
            return new PluginException(name, PluginException.Reason.TRAP, "the module trapped: " + t.getMessage(), t);
        }
        if (t instanceof Error e) {
            throw e;
        }
        return new PluginException(name, PluginException.Reason.TRAP, "the call failed: " + t, t);
    }

    private static boolean checkImports(String name, WasmModule module) {
        ImportSection section = module.importSection();
        boolean log = false;
        List<String> refused = new ArrayList<>();
        for (int i = 0; i < section.importCount(); i++) {
            Import imp = section.getImport(i);
            String label = imp.module() + "." + imp.name();
            if (imp instanceof FunctionImport function && IMPORT_MODULE.equals(imp.module())
                    && LOG_IMPORT.equals(imp.name())) {
                FunctionType type = module.typeSection().getType(function.typeIndex());
                if (!LOG_TYPE.equals(type)) {
                    refused.add(label + " with type " + type + " (expected (i32, i32, i32) -> ())");
                    continue;
                }
                log = true;
                continue;
            }
            refused.add(imp.importType() == ExternalType.FUNCTION ? label
                    : label + " (" + imp.importType().name().toLowerCase(java.util.Locale.ROOT) + ")");
        }
        if (!refused.isEmpty()) {
            throw invalid(name, "the module imports " + String.join(", ", refused) + ", which is not granted: ABI v1"
                    + " grants no WASI or other imports, only " + IMPORT_MODULE + "." + LOG_IMPORT + "(i32, i32, i32)");
        }
        return log;
    }

    private static void checkMemory(String name, WasmModule module, PluginLimits limits) {
        MemorySection memories = module.memorySection().orElse(null);
        if (memories == null || memories.memoryCount() != 1) {
            throw invalid(name, "the module must define exactly one memory");
        }
        MemoryLimits declared = memories.getMemory(0).limits();
        if (declared.shared()) {
            throw invalid(name, "the module declares a shared memory; ABI v1 runs single-threaded modules only");
        }
        if (declared.initialPages() > limits.maxMemoryPages()) {
            throw invalid(name, "the module asks for " + declared.initialPages() + " initial memory pages, over the "
                    + limits.maxMemoryPages() + "-page (" + limits.maxMemoryBytes() / (1024 * 1024) + " MB) limit");
        }
    }

    private static void checkExports(String name, WasmModule module) {
        ExportSection section = module.exportSection();
        Map<String, Export> exports = new HashMap<>();
        for (int i = 0; i < section.exportCount(); i++) {
            Export export = section.getExport(i);
            exports.put(export.name(), export);
        }
        Export memory = exports.get(MEMORY_EXPORT);
        if (memory == null || memory.exportType() != ExternalType.MEMORY) {
            throw invalid(name, "the module must export its memory as \"" + MEMORY_EXPORT + "\"");
        }
        int importedFunctions = (int) module.importSection().count(ExternalType.FUNCTION);
        requireFunction(name, module, exports, ALLOC_EXPORT, ALLOC_TYPE, importedFunctions, "(i32) -> i32");
        requireFunction(name, module, exports, DEALLOC_EXPORT, DEALLOC_TYPE, importedFunctions, "(i32, i32) -> ()");
        requireFunction(name, module, exports, PARSE_EXPORT, PARSE_TYPE, importedFunctions, "(i32, i32) -> i32");
    }

    private static void requireFunction(String name, WasmModule module, Map<String, Export> exports, String export,
            FunctionType expected, int importedFunctions, String signature) {
        Export found = exports.get(export);
        if (found == null || found.exportType() != ExternalType.FUNCTION) {
            throw invalid(name, "the module must export the function " + export + signature);
        }
        int index = found.index();
        FunctionType actual = index < importedFunctions ? LOG_TYPE
                : module.functionSection().getFunctionType(index - importedFunctions, module.typeSection());
        if (!expected.equals(actual)) {
            throw invalid(name, "the export " + export + " has type " + actual + ", expected " + signature);
        }
    }

    private static PluginException invalid(String name, String message) {
        return new PluginException(name, PluginException.Reason.INVALID_MODULE, message);
    }
}

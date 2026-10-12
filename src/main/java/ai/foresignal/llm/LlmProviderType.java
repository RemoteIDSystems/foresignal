package ai.foresignal.llm;

/** The provider implementations (OpenAPI {@code LlmProvider.type}). */
public enum LlmProviderType {
    /** Anthropic Claude through the official Java SDK. No embeddings endpoint. */
    ANTHROPIC("anthropic", "https://api.anthropic.com"),
    /** Any OpenAI-compatible API: OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp, LocalAI. */
    OPENAI_COMPATIBLE("openai-compatible", null),
    /** A local Ollama server; the compose {@code local-llm} profile serves it at {@code http://ollama:11434}. */
    OLLAMA("ollama", "http://ollama:11434");

    private final String wireValue;
    private final String defaultBaseUrl;

    LlmProviderType(String wireValue, String defaultBaseUrl) {
        this.wireValue = wireValue;
        this.defaultBaseUrl = defaultBaseUrl;
    }

    /** The wire value, for example {@code openai-compatible}. */
    public String wireValue() {
        return wireValue;
    }

    /** The {@code baseUrl} used when none is given, or null when one is required. */
    public String defaultBaseUrl() {
        return defaultBaseUrl;
    }

    /** True when the provider has an embeddings endpoint. */
    public boolean supportsEmbeddings() {
        return this != ANTHROPIC;
    }

    /** Parses a wire value; null when unknown. */
    public static LlmProviderType fromWire(String value) {
        for (LlmProviderType type : values()) {
            if (type.wireValue.equals(value)) {
                return type;
            }
        }
        return null;
    }
}

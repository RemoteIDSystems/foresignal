from enum import StrEnum


class LlmProviderInputType(StrEnum):
    ANTHROPIC = "anthropic"
    OLLAMA = "ollama"
    OPENAI_COMPATIBLE = "openai-compatible"

    def __str__(self) -> str:
        return str(self.value)

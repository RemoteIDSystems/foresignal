from enum import StrEnum


class ContextSummaryNarrativeType0TextOrigin(StrEnum):
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

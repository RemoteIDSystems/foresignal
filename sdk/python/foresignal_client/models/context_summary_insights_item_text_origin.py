from enum import StrEnum


class ContextSummaryInsightsItemTextOrigin(StrEnum):
    DETERMINISTIC = "deterministic"
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

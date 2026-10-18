from enum import StrEnum


class HypothesisSetDraftTextOrigin(StrEnum):
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

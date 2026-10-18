from enum import StrEnum


class BriefingInputMode(StrEnum):
    DIGEST = "digest"
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

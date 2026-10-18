from enum import StrEnum


class ChronicleSuggestionTextOrigin(StrEnum):
    DETERMINISTIC = "deterministic"
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

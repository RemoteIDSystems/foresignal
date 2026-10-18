from enum import StrEnum


class ObjectEnvelopeTextOrigin(StrEnum):
    DETERMINISTIC = "deterministic"
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

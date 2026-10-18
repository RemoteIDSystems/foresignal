from enum import StrEnum


class AssessmentSectionsItemTextOrigin(StrEnum):
    DETERMINISTIC = "deterministic"
    LLM = "llm"
    MIXED = "mixed"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class IntelligenceEventSpeculationNarrativeOriginType1(StrEnum):
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)

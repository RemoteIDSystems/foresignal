from enum import StrEnum


class HypothesisInputType(StrEnum):
    EXPLANATION = "explanation"
    OTHER = "other"
    OUTCOME = "outcome"
    SPECULATIVE_LINK = "speculative-link"

    def __str__(self) -> str:
        return str(self.value)

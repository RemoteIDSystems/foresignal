from enum import StrEnum


class HypothesisSetInputKind(StrEnum):
    EXCLUSIVE = "exclusive"
    INDEPENDENT = "independent"

    def __str__(self) -> str:
        return str(self.value)

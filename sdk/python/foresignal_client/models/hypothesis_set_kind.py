from enum import StrEnum


class HypothesisSetKind(StrEnum):
    EXCLUSIVE = "exclusive"
    INDEPENDENT = "independent"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class ForecasterComparisonMetric(StrEnum):
    BRIER = "brier"

    def __str__(self) -> str:
        return str(self.value)

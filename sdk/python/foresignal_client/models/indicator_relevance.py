from enum import StrEnum


class IndicatorRelevance(StrEnum):
    H = "H"
    L = "L"
    M = "M"

    def __str__(self) -> str:
        return str(self.value)

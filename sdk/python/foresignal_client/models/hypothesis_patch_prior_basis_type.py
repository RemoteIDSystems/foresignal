from enum import StrEnum


class HypothesisPatchPriorBasisType(StrEnum):
    ANALOGS = "analogs"
    BASE_RATE = "base-rate"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)

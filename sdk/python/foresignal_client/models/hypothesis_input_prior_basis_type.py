from enum import StrEnum


class HypothesisInputPriorBasisType(StrEnum):
    ANALOGS = "analogs"
    BASE_RATE = "base-rate"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)

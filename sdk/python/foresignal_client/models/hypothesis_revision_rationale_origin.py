from enum import StrEnum


class HypothesisRevisionRationaleOrigin(StrEnum):
    ANALYST = "analyst"
    DETERMINISTIC = "deterministic"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)

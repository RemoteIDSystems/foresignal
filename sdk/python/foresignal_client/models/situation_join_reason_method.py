from enum import StrEnum


class SituationJoinReasonMethod(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    LINKER = "linker"
    OPERATOR = "operator"
    SEED = "seed"

    def __str__(self) -> str:
        return str(self.value)

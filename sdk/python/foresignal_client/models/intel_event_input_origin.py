from enum import StrEnum


class IntelEventInputOrigin(StrEnum):
    AGENT = "agent"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)

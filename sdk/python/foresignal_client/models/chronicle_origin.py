from enum import StrEnum


class ChronicleOrigin(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)

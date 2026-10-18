from enum import StrEnum


class IndicatorOrigin(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)

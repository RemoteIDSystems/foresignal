from enum import StrEnum


class IntelligenceEventOrigin(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    OPERATOR = "operator"
    RECIPE = "recipe"

    def __str__(self) -> str:
        return str(self.value)

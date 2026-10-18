from enum import StrEnum


class ForecastQuestionOrigin(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    OPERATOR = "operator"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)

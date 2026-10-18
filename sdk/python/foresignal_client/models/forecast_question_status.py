from enum import StrEnum


class ForecastQuestionStatus(StrEnum):
    ANNULLED = "annulled"
    CLOSED = "closed"
    OPEN = "open"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)

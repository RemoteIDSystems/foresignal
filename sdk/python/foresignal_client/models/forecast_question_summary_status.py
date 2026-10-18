from enum import StrEnum


class ForecastQuestionSummaryStatus(StrEnum):
    ANNULLED = "annulled"
    CLOSED = "closed"
    OPEN = "open"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)

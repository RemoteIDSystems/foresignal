from enum import StrEnum


class ForecastQuestionEventEvent(StrEnum):
    QUESTION_ANNULLED = "question.annulled"
    QUESTION_CLOSED = "question.closed"
    QUESTION_OPENED = "question.opened"
    QUESTION_RESOLVED = "question.resolved"

    def __str__(self) -> str:
        return str(self.value)

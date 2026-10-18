from enum import StrEnum


class AssessmentFeedbackRating(StrEnum):
    NOT_USEFUL = "not_useful"
    USEFUL = "useful"
    WRONG = "wrong"

    def __str__(self) -> str:
        return str(self.value)

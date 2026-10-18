from enum import StrEnum


class HypothesisReviewInputCause(StrEnum):
    OPERATOR = "operator"
    REVIEW = "review"

    def __str__(self) -> str:
        return str(self.value)

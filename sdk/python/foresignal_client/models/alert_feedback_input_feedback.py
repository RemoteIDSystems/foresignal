from enum import StrEnum


class AlertFeedbackInputFeedback(StrEnum):
    FALSE_POSITIVE = "false_positive"
    LATE = "late"
    NOISE = "noise"
    USEFUL = "useful"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class AlertTransitionInputFeedbackType3Type1(StrEnum):
    FALSE_POSITIVE = "false_positive"
    LATE = "late"
    NOISE = "noise"
    USEFUL = "useful"

    def __str__(self) -> str:
        return str(self.value)

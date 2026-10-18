from enum import StrEnum


class EventStudyResultTransform(StrEnum):
    ANSCOMBE_RESIDUAL = "anscombe-residual"
    LOG_LEVEL_STANDARDISED = "log-level-standardised"
    LOG_RETURN = "log-return"
    WEEKLY_DIFFERENCE_STANDARDISED = "weekly-difference-standardised"

    def __str__(self) -> str:
        return str(self.value)

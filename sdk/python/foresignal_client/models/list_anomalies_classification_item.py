from enum import StrEnum


class ListAnomaliesClassificationItem(StrEnum):
    ACTIVITY_DOWN = "activity-down"
    ACTIVITY_UP = "activity-up"
    COVERAGE_DROP = "coverage-drop"
    INSUFFICIENT_COVERAGE = "insufficient-coverage"
    LEARNING = "learning"
    NORMAL = "normal"

    def __str__(self) -> str:
        return str(self.value)

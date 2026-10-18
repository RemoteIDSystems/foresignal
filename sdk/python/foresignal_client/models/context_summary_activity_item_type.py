from enum import StrEnum


class ContextSummaryActivityItemType(StrEnum):
    ALERT = "alert"
    FINDING = "finding"
    RECORD = "record"
    SITUATION_CHANGE = "situation-change"

    def __str__(self) -> str:
        return str(self.value)

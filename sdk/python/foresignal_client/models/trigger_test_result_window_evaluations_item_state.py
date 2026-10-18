from enum import StrEnum


class TriggerTestResultWindowEvaluationsItemState(StrEnum):
    ACTIVE = "active"
    CLEAR = "clear"
    LEARNING = "learning"
    UNKNOWN = "unknown"

    def __str__(self) -> str:
        return str(self.value)

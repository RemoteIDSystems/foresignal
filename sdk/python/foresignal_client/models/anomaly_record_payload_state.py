from enum import StrEnum


class AnomalyRecordPayloadState(StrEnum):
    ACTIVE = "active"
    CLEAR = "clear"
    LEARNING = "learning"
    UNKNOWN = "unknown"

    def __str__(self) -> str:
        return str(self.value)

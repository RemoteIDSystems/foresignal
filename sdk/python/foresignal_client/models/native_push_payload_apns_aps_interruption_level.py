from enum import StrEnum


class NativePushPayloadApnsApsInterruptionLevel(StrEnum):
    ACTIVE = "active"
    PASSIVE = "passive"
    TIME_SENSITIVE = "time-sensitive"

    def __str__(self) -> str:
        return str(self.value)

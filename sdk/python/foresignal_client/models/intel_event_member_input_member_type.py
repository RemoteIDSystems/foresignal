from enum import StrEnum


class IntelEventMemberInputMemberType(StrEnum):
    ALERT = "alert"
    ANOMALY = "anomaly"
    EVENT = "event"
    RECORD = "record"

    def __str__(self) -> str:
        return str(self.value)

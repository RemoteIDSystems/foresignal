from enum import StrEnum


class IntelEventMemberMemberType(StrEnum):
    ALERT = "alert"
    ANOMALY = "anomaly"
    EVENT = "event"
    RECORD = "record"

    def __str__(self) -> str:
        return str(self.value)

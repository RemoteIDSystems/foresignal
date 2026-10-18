from enum import StrEnum


class IndicatorSignalSituationType0StatusType3Type1(StrEnum):
    ACTIVE = "active"
    CLOSED = "closed"
    DORMANT = "dormant"
    EMERGING = "emerging"

    def __str__(self) -> str:
        return str(self.value)

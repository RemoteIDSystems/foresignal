from enum import StrEnum


class StreamEventsSituationsStatus(StrEnum):
    ACTIVE = "active"
    CLOSED = "closed"
    DORMANT = "dormant"
    EMERGING = "emerging"

    def __str__(self) -> str:
        return str(self.value)

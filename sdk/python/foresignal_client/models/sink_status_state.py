from enum import StrEnum


class SinkStatusState(StrEnum):
    FAILING = "FAILING"
    RESYNCING = "RESYNCING"
    RUNNING = "RUNNING"
    STOPPED = "STOPPED"

    def __str__(self) -> str:
        return str(self.value)

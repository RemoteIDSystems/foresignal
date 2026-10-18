from enum import StrEnum


class SystemInfoIngestWriterState(StrEnum):
    CLOCK_BEHIND = "CLOCK_BEHIND"
    LOCK_LOST = "LOCK_LOST"
    RUNNING = "RUNNING"
    STOPPED = "STOPPED"

    def __str__(self) -> str:
        return str(self.value)

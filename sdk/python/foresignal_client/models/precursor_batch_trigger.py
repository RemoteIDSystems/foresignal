from enum import StrEnum


class PrecursorBatchTrigger(StrEnum):
    CYCLE = "cycle"
    MANUAL = "manual"
    SCHEDULE = "schedule"

    def __str__(self) -> str:
        return str(self.value)

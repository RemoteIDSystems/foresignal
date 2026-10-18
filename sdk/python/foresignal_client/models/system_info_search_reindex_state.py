from enum import StrEnum


class SystemInfoSearchReindexState(StrEnum):
    BACKFILLING = "backfilling"
    IDLE = "idle"
    INDEXING = "indexing"
    SWAPPING = "swapping"

    def __str__(self) -> str:
        return str(self.value)

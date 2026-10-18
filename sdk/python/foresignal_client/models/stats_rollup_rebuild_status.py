from enum import StrEnum


class StatsRollupRebuildStatus(StrEnum):
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)

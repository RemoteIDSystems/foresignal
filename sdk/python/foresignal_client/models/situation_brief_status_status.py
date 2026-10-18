from enum import StrEnum


class SituationBriefStatusStatus(StrEnum):
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)

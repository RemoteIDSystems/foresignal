from enum import StrEnum


class StormTrackPayloadStatus(StrEnum):
    ACTIVE = "active"
    ENDED = "ended"

    def __str__(self) -> str:
        return str(self.value)

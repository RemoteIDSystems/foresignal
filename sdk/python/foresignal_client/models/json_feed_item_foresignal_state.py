from enum import StrEnum


class JsonFeedItemForesignalState(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    FIRING = "firing"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class SinkTypeKind(StrEnum):
    CHANGE_FEED = "CHANGE_FEED"
    STATE_MIRROR = "STATE_MIRROR"

    def __str__(self) -> str:
        return str(self.value)

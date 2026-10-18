from enum import StrEnum


class PresenceEventChangeType0Type(StrEnum):
    JOINED = "joined"
    LEFT = "left"

    def __str__(self) -> str:
        return str(self.value)

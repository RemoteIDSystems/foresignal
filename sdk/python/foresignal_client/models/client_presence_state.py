from enum import StrEnum


class ClientPresenceState(StrEnum):
    ACTIVE = "active"
    BACKGROUND = "background"
    IDLE = "idle"

    def __str__(self) -> str:
        return str(self.value)

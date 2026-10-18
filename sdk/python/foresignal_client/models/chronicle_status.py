from enum import StrEnum


class ChronicleStatus(StrEnum):
    ACTIVE = "active"
    ARCHIVED = "archived"
    PAUSED = "paused"
    PROPOSED = "proposed"
    STOPPED = "stopped"

    def __str__(self) -> str:
        return str(self.value)

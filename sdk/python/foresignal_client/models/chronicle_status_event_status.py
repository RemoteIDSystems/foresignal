from enum import StrEnum


class ChronicleStatusEventStatus(StrEnum):
    ACTIVE = "active"
    ARCHIVED = "archived"
    PAUSED = "paused"
    PROPOSED = "proposed"
    STOPPED = "stopped"

    def __str__(self) -> str:
        return str(self.value)

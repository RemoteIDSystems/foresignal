from enum import StrEnum


class EscalationState(StrEnum):
    ACTIVE = "active"
    EXHAUSTED = "exhausted"
    STOPPED = "stopped"

    def __str__(self) -> str:
        return str(self.value)

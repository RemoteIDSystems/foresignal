from enum import StrEnum


class CycleRunTrigger(StrEnum):
    MANUAL = "manual"
    MISSED = "missed"
    SCHEDULE = "schedule"
    SITUATION = "situation"

    def __str__(self) -> str:
        return str(self.value)

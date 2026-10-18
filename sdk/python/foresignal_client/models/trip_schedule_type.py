from enum import StrEnum


class TripScheduleType(StrEnum):
    ONCE = "once"
    RECURRING = "recurring"

    def __str__(self) -> str:
        return str(self.value)

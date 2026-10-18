from enum import StrEnum


class ExerciseRunCreateSpeed(StrEnum):
    MAX = "max"
    REALTIME = "realtime"
    X10 = "x10"
    X60 = "x60"

    def __str__(self) -> str:
        return str(self.value)

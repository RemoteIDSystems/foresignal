from enum import StrEnum


class CustomSourceInputObservedAtMode(StrEnum):
    RECEIVED = "RECEIVED"
    UPSTREAM = "UPSTREAM"

    def __str__(self) -> str:
        return str(self.value)

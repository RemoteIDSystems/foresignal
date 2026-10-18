from enum import StrEnum


class SituationInputStatus(StrEnum):
    ACTIVE = "active"
    EMERGING = "emerging"

    def __str__(self) -> str:
        return str(self.value)

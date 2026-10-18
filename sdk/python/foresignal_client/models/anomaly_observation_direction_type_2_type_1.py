from enum import StrEnum


class AnomalyObservationDirectionType2Type1(StrEnum):
    DOWN = "down"
    UP = "up"

    def __str__(self) -> str:
        return str(self.value)

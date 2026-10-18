from enum import StrEnum


class IndicatorSignalSeriesType0DirectionType2Type1(StrEnum):
    ANY = "any"
    DOWN = "down"
    UP = "up"

    def __str__(self) -> str:
        return str(self.value)

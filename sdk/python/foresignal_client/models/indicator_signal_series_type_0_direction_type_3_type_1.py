from enum import StrEnum


class IndicatorSignalSeriesType0DirectionType3Type1(StrEnum):
    ANY = "any"
    DOWN = "down"
    UP = "up"

    def __str__(self) -> str:
        return str(self.value)

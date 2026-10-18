from enum import StrEnum


class SeriesInfoFrequency(StrEnum):
    P1D = "P1D"
    P1M = "P1M"
    P1W = "P1W"
    P1Y = "P1Y"
    P3M = "P3M"
    PT15M = "PT15M"
    PT15S = "PT15S"
    PT1H = "PT1H"
    PT30M = "PT30M"

    def __str__(self) -> str:
        return str(self.value)

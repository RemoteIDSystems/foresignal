from enum import StrEnum


class SeriesForecastUnits(StrEnum):
    NATIVE = "native"
    STANDARDISED = "standardised"

    def __str__(self) -> str:
        return str(self.value)

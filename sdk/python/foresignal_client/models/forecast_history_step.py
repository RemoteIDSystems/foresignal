from enum import StrEnum


class ForecastHistoryStep(StrEnum):
    P1D = "P1D"

    def __str__(self) -> str:
        return str(self.value)

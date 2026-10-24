from enum import StrEnum


class ForecastNumericCalibrationLabel(StrEnum):
    INSUFFICIENT = "insufficient"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)

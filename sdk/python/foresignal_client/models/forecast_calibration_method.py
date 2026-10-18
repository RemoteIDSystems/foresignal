from enum import StrEnum


class ForecastCalibrationMethod(StrEnum):
    BINS = "bins"
    CORP = "corp"

    def __str__(self) -> str:
        return str(self.value)

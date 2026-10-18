from enum import StrEnum


class ProductTemporalType(StrEnum):
    FORECAST = "forecast"
    OBSERVATION = "observation"
    PASS = "pass"

    def __str__(self) -> str:
        return str(self.value)

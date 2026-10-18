from enum import StrEnum


class BacktestRequestType(StrEnum):
    PRECURSOR = "precursor"
    QUALITY = "quality"
    RECIPE = "recipe"
    RULE = "rule"

    def __str__(self) -> str:
        return str(self.value)

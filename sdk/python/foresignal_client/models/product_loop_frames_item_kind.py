from enum import StrEnum


class ProductLoopFramesItemKind(StrEnum):
    FORECAST = "forecast"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)

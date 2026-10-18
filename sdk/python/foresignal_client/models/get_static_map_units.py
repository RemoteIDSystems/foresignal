from enum import StrEnum


class GetStaticMapUnits(StrEnum):
    IMPERIAL = "imperial"
    METRIC = "metric"

    def __str__(self) -> str:
        return str(self.value)

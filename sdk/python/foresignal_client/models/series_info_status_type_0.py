from enum import StrEnum


class SeriesInfoStatusType0(StrEnum):
    ESTIMATED = "estimated"
    FINAL = "final"
    FORECAST = "forecast"
    PRELIMINARY = "preliminary"

    def __str__(self) -> str:
        return str(self.value)

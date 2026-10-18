from enum import StrEnum


class SeriesPointStatus(StrEnum):
    ESTIMATED = "estimated"
    FINAL = "final"
    FORECAST = "forecast"
    PRELIMINARY = "preliminary"

    def __str__(self) -> str:
        return str(self.value)

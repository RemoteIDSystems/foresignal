from enum import StrEnum


class WeatherContextStatus(StrEnum):
    OK = "ok"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)

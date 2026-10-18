from enum import StrEnum


class AskCitationType(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    RECORD = "record"
    WEATHER = "weather"

    def __str__(self) -> str:
        return str(self.value)

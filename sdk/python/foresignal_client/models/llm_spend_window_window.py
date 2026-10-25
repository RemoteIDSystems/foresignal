from enum import StrEnum


class LlmSpendWindowWindow(StrEnum):
    DAY = "day"
    HOUR = "hour"
    MONTH = "month"

    def __str__(self) -> str:
        return str(self.value)

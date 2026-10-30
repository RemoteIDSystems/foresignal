from enum import StrEnum


class TranslationSettingsBudgetWindow(StrEnum):
    DAY = "day"
    HOUR = "hour"
    MONTH = "month"

    def __str__(self) -> str:
        return str(self.value)

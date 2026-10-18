from enum import StrEnum


class IntelBoardLatestAssessmentsItemCadence(StrEnum):
    DAILY = "daily"
    MONTHLY = "monthly"
    WEEKLY = "weekly"

    def __str__(self) -> str:
        return str(self.value)

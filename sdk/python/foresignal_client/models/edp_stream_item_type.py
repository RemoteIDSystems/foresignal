from enum import StrEnum


class EdpStreamItemType(StrEnum):
    DOCUMENT = "document"
    EVENT = "event"
    FEATURE = "feature"
    FORECAST = "forecast"
    OBSERVATION = "observation"

    def __str__(self) -> str:
        return str(self.value)

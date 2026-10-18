from enum import StrEnum


class QueryFdsnEventsFormat(StrEnum):
    TEXT = "text"
    XML = "xml"

    def __str__(self) -> str:
        return str(self.value)

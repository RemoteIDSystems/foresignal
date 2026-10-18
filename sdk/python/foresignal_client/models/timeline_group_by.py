from enum import StrEnum


class TimelineGroupBy(StrEnum):
    KIND = "kind"
    NONE = "none"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)

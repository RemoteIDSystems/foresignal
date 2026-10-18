from enum import StrEnum


class GetTimelineGroupBy(StrEnum):
    KIND = "kind"
    NONE = "none"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)

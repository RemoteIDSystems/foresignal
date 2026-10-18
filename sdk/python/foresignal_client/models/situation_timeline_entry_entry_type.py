from enum import StrEnum


class SituationTimelineEntryEntryType(StrEnum):
    LOG = "log"
    MEMBER = "member"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class ReportRevisionStatus(StrEnum):
    PUBLISHED = "published"
    RETRACTED = "retracted"

    def __str__(self) -> str:
        return str(self.value)

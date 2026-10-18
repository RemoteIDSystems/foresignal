from enum import StrEnum


class ListReportsStatus(StrEnum):
    DRAFT = "draft"
    PUBLISHED = "published"
    RETRACTED = "retracted"
    REVIEW = "review"

    def __str__(self) -> str:
        return str(self.value)

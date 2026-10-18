from enum import StrEnum


class ReportPatchStatus(StrEnum):
    DRAFT = "draft"
    REVIEW = "review"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class AnnotationVerdictType1(StrEnum):
    CONFIRMED = "confirmed"
    DUPLICATE_OF = "duplicate-of"
    NEEDS_REVIEW = "needs-review"
    REJECTED = "rejected"

    def __str__(self) -> str:
        return str(self.value)

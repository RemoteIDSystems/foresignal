from enum import StrEnum


class AnnotationInputVerdict(StrEnum):
    CONFIRMED = "confirmed"
    DUPLICATE_OF = "duplicate-of"
    NEEDS_REVIEW = "needs-review"
    REJECTED = "rejected"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class AnnotationKind(StrEnum):
    NOTE = "note"
    VERDICT = "verdict"

    def __str__(self) -> str:
        return str(self.value)

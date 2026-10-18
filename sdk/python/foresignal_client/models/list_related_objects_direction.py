from enum import StrEnum


class ListRelatedObjectsDirection(StrEnum):
    BOTH = "both"
    IN = "in"
    OUT = "out"

    def __str__(self) -> str:
        return str(self.value)

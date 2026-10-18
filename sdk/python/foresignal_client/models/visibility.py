from enum import StrEnum


class Visibility(StrEnum):
    INSTANCE = "instance"
    PRIVATE = "private"
    SHARED = "shared"

    def __str__(self) -> str:
        return str(self.value)

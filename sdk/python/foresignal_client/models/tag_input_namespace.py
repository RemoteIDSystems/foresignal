from enum import StrEnum


class TagInputNamespace(StrEnum):
    PERSONAL = "personal"
    SHARED = "shared"

    def __str__(self) -> str:
        return str(self.value)

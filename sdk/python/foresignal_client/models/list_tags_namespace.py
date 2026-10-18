from enum import StrEnum


class ListTagsNamespace(StrEnum):
    PERSONAL = "personal"
    SHARED = "shared"

    def __str__(self) -> str:
        return str(self.value)

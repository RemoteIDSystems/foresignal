from enum import StrEnum


class SearchMode(StrEnum):
    HYBRID = "hybrid"
    SEMANTIC = "semantic"
    TEXT = "text"

    def __str__(self) -> str:
        return str(self.value)

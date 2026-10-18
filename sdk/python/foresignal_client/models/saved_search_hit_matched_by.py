from enum import StrEnum


class SavedSearchHitMatchedBy(StrEnum):
    FILTER = "filter"
    HYBRID = "hybrid"
    SEMANTIC = "semantic"
    TEXT = "text"

    def __str__(self) -> str:
        return str(self.value)

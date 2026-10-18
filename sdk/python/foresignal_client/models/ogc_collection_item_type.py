from enum import StrEnum


class OgcCollectionItemType(StrEnum):
    FEATURE = "feature"

    def __str__(self) -> str:
        return str(self.value)

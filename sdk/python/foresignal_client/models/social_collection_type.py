from enum import StrEnum


class SocialCollectionType(StrEnum):
    COMPLETE = "complete"
    PAGED = "paged"
    SAMPLED = "sampled"

    def __str__(self) -> str:
        return str(self.value)

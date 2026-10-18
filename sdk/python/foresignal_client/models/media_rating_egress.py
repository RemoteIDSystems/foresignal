from enum import StrEnum


class MediaRatingEgress(StrEnum):
    LOCAL_ONLY = "LOCAL_ONLY"
    OPEN = "OPEN"

    def __str__(self) -> str:
        return str(self.value)

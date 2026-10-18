from enum import StrEnum


class MediaReuseEventReason(StrEnum):
    EARLIER = "earlier"
    ELSEWHERE = "elsewhere"

    def __str__(self) -> str:
        return str(self.value)

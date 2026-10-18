from enum import StrEnum


class HistoryFrameRemovalsItemReason(StrEnum):
    EXPIRED = "expired"
    LEFT = "left"
    TOMBSTONE = "tombstone"

    def __str__(self) -> str:
        return str(self.value)

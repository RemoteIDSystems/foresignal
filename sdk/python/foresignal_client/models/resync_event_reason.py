from enum import StrEnum


class ResyncEventReason(StrEnum):
    GAP = "gap"
    LIVE_CAP = "live_cap"
    LIVE_DROPPED = "live_dropped"

    def __str__(self) -> str:
        return str(self.value)

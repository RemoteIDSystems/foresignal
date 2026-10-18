from enum import StrEnum


class ResetEventReason(StrEnum):
    BUFFER_OVERFLOW = "buffer_overflow"
    REPLAY_CAP = "replay_cap"
    RESYNC_REQUIRED = "resync_required"

    def __str__(self) -> str:
        return str(self.value)

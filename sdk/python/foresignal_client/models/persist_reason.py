from enum import StrEnum


class PersistReason(StrEnum):
    CHANGE = "CHANGE"
    FIRST = "FIRST"
    HEARTBEAT = "HEARTBEAT"
    KEYFRAME = "KEYFRAME"
    REAPPEAR = "REAPPEAR"
    SAMPLE = "SAMPLE"
    SIGNIFICANT = "SIGNIFICANT"
    TOMBSTONE = "TOMBSTONE"
    TRIGGER_EDGE = "TRIGGER_EDGE"

    def __str__(self) -> str:
        return str(self.value)

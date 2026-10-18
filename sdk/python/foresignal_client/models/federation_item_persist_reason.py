from enum import StrEnum


class FederationItemPersistReason(StrEnum):
    CHANGE = "CHANGE"
    FIRST = "FIRST"
    HEARTBEAT = "HEARTBEAT"
    KEYFRAME = "KEYFRAME"
    REAPPEAR = "REAPPEAR"
    SAMPLE = "SAMPLE"
    SIGNIFICANT = "SIGNIFICANT"
    SNAPSHOT = "SNAPSHOT"
    TOMBSTONE = "TOMBSTONE"
    TRIGGER_EDGE = "TRIGGER_EDGE"

    def __str__(self) -> str:
        return str(self.value)

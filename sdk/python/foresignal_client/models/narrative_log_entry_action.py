from enum import StrEnum


class NarrativeLogEntryAction(StrEnum):
    CREATED = "created"
    MERGED = "merged"
    MERGED_INTO = "merged-into"
    SPLIT = "split"
    SPLIT_FROM = "split-from"
    STATE_CHANGED = "state-changed"
    STATUS_CHANGED = "status-changed"
    TITLE_CHANGED = "title-changed"

    def __str__(self) -> str:
        return str(self.value)

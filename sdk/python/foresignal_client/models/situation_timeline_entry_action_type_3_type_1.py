from enum import StrEnum


class SituationTimelineEntryActionType3Type1(StrEnum):
    BRIEF = "brief"
    CREATED = "created"
    MEMBER_ADDED = "member_added"
    MEMBER_REMOVED = "member_removed"
    MERGED = "merged"
    RENAMED = "renamed"
    SEVERITY = "severity"
    SPLIT = "split"
    STATUS = "status"
    THEME = "theme"

    def __str__(self) -> str:
        return str(self.value)

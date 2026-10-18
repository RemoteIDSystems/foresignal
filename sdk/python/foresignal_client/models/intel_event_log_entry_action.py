from enum import StrEnum


class IntelEventLogEntryAction(StrEnum):
    CREATED = "created"
    EDITED = "edited"
    MEMBER_ADDED = "member_added"
    MEMBER_REMOVED = "member_removed"
    MERGED = "merged"
    REOPENED = "reopened"
    STATUS = "status"

    def __str__(self) -> str:
        return str(self.value)

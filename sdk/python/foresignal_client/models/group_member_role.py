from enum import StrEnum


class GroupMemberRole(StrEnum):
    MANAGER = "manager"
    MEMBER = "member"

    def __str__(self) -> str:
        return str(self.value)

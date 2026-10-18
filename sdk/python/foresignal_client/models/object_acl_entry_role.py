from enum import StrEnum


class ObjectAclEntryRole(StrEnum):
    COMMENTER = "commenter"
    EDITOR = "editor"
    OWNER = "owner"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

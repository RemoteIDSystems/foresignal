from enum import StrEnum


class SharedItemRole(StrEnum):
    COMMENTER = "commenter"
    EDITOR = "editor"
    OWNER = "owner"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

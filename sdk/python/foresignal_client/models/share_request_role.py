from enum import StrEnum


class ShareRequestRole(StrEnum):
    COMMENTER = "commenter"
    EDITOR = "editor"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

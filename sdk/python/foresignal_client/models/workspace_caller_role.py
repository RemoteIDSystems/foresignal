from enum import StrEnum


class WorkspaceCallerRole(StrEnum):
    EDITOR = "editor"
    OWNER = "owner"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

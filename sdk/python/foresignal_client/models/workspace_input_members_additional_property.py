from enum import StrEnum


class WorkspaceInputMembersAdditionalProperty(StrEnum):
    EDITOR = "editor"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

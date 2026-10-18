from enum import StrEnum


class AccessGrantInputRole(StrEnum):
    COMMENTER = "commenter"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

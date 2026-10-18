from enum import StrEnum


class AccessRequestGrantType0RoleType1(StrEnum):
    COMMENTER = "commenter"
    VIEWER = "viewer"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class EdpPullAuth(StrEnum):
    BASIC = "basic"
    BEARER = "bearer"
    HEADER = "header"
    NONE = "none"

    def __str__(self) -> str:
        return str(self.value)

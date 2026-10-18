from enum import StrEnum


class ListSituationMembersRole(StrEnum):
    CONTEXT = "context"
    CORE = "core"

    def __str__(self) -> str:
        return str(self.value)

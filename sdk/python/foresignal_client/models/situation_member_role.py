from enum import StrEnum


class SituationMemberRole(StrEnum):
    CONTEXT = "context"
    CORE = "core"

    def __str__(self) -> str:
        return str(self.value)

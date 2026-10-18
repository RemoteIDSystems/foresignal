from enum import StrEnum


class ListInvestigationsScope(StrEnum):
    ALL = "all"
    MINE = "mine"
    SHARED = "shared"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class ListPrecursorsStatusItem(StrEnum):
    CANDIDATE = "candidate"
    RETIRED = "retired"
    STALE = "stale"
    VALIDATED = "validated"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class SituationMemberCorroboration(StrEnum):
    CORROBORATED = "corroborated"
    NA = "n/a"
    UNCORROBORATED = "uncorroborated"
    UNKNOWN = "unknown"

    def __str__(self) -> str:
        return str(self.value)

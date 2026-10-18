from enum import StrEnum


class IngestPolicyAllMode(StrEnum):
    ALL = "ALL"

    def __str__(self) -> str:
        return str(self.value)

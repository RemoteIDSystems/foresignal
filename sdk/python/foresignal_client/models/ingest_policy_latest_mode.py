from enum import StrEnum


class IngestPolicyLatestMode(StrEnum):
    LATEST = "LATEST"

    def __str__(self) -> str:
        return str(self.value)

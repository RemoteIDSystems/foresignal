from enum import StrEnum


class FederationItemHistoryTier(StrEnum):
    STANDARD = "STANDARD"
    TRACK = "TRACK"

    def __str__(self) -> str:
        return str(self.value)

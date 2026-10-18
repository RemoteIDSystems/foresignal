from enum import StrEnum


class FederationInfoCapabilitiesItem(StrEnum):
    CHANGES = "changes"
    INGEST = "ingest"
    SNAPSHOT = "snapshot"
    STREAM = "stream"

    def __str__(self) -> str:
        return str(self.value)

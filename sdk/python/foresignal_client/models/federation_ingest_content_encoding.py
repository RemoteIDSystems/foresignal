from enum import StrEnum


class FederationIngestContentEncoding(StrEnum):
    GZIP = "gzip"

    def __str__(self) -> str:
        return str(self.value)

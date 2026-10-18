from enum import StrEnum


class IngestRecordsFormatContentEncoding(StrEnum):
    GZIP = "gzip"
    IDENTITY = "identity"

    def __str__(self) -> str:
        return str(self.value)

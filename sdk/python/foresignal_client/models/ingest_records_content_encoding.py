from enum import StrEnum


class IngestRecordsContentEncoding(StrEnum):
    GZIP = "gzip"
    IDENTITY = "identity"

    def __str__(self) -> str:
        return str(self.value)

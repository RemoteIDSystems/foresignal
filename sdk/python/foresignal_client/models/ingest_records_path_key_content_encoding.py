from enum import StrEnum


class IngestRecordsPathKeyContentEncoding(StrEnum):
    GZIP = "gzip"
    IDENTITY = "identity"

    def __str__(self) -> str:
        return str(self.value)

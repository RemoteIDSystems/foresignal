from enum import StrEnum


class MediaProviderLastImportStatusType1(StrEnum):
    FAILED = "failed"
    OK = "ok"
    PARTIAL = "partial"

    def __str__(self) -> str:
        return str(self.value)

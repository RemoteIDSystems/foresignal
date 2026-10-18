from enum import StrEnum


class ExportLicenceSummaryExcludedItemReason(StrEnum):
    LICENCE_FORBIDDEN = "licence-forbidden"
    LICENCE_LOCAL_ONLY = "licence-local-only"
    PRIVATE = "private"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class BriefingRunBreakingType0Severity(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

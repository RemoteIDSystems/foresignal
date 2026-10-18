from enum import StrEnum


class QuietHoursBypassSeverityType1(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

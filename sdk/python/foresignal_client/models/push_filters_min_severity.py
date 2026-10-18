from enum import StrEnum


class PushFiltersMinSeverity(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

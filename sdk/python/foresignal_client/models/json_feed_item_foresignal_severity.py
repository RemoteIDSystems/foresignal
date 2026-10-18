from enum import StrEnum


class JsonFeedItemForesignalSeverity(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

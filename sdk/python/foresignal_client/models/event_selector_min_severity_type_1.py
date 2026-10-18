from enum import StrEnum


class EventSelectorMinSeverityType1(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    NOTABLE = "notable"
    SIGNIFICANT = "significant"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

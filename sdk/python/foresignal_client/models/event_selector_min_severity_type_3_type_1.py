from enum import StrEnum


class EventSelectorMinSeverityType3Type1(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    NOTABLE = "notable"
    SIGNIFICANT = "significant"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

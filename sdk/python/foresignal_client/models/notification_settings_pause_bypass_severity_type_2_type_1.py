from enum import StrEnum


class NotificationSettingsPauseBypassSeverityType2Type1(StrEnum):
    CRITICAL = "critical"
    INFO = "info"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class NwsAlertPayloadSeverity(StrEnum):
    EXTREME = "Extreme"
    MINOR = "Minor"
    MODERATE = "Moderate"
    SEVERE = "Severe"
    UNKNOWN = "Unknown"

    def __str__(self) -> str:
        return str(self.value)

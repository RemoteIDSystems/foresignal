from enum import StrEnum


class AlertSignalType(StrEnum):
    ACK_FAST = "ACK_FAST"
    FLAP = "FLAP"
    SNOOZED = "SNOOZED"
    UNOPENED = "UNOPENED"

    def __str__(self) -> str:
        return str(self.value)

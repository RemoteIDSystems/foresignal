from enum import StrEnum


class AlertSuppressedReasonType2Type1(StrEnum):
    ALERT_SNOOZED = "alert_snoozed"
    RULE_SNOOZED = "rule_snoozed"

    def __str__(self) -> str:
        return str(self.value)

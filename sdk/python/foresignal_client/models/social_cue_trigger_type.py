from enum import StrEnum


class SocialCueTriggerType(StrEnum):
    ALERT = "alert"
    ANOMALY = "anomaly"
    CANONICAL_EVENT = "canonical-event"
    FINDING = "finding"
    MANUAL = "manual"
    SITUATION_ACTIVATED = "situation-activated"
    SITUATION_ESCALATED = "situation-escalated"

    def __str__(self) -> str:
        return str(self.value)

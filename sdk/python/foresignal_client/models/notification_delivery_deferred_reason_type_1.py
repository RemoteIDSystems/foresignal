from enum import StrEnum


class NotificationDeliveryDeferredReasonType1(StrEnum):
    AIRTIME = "airtime"
    ENRICHMENT = "enrichment"
    GROUPING = "grouping"
    PAUSED = "paused"
    PRESENCE = "presence"
    QUIET_HOURS = "quiet_hours"

    def __str__(self) -> str:
        return str(self.value)

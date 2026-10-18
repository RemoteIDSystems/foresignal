from enum import StrEnum


class FindingEventFindingFindingType(StrEnum):
    ATTENTION_GAP = "attention-gap"
    CHANGE_POINT = "change-point"
    CO_OCCURRENCE = "co-occurrence"
    FORECAST_EXCEEDANCE = "forecast-exceedance"
    MEDIA_REUSE = "media-reuse"
    OUTLOOK_EXCEEDANCE = "outlook-exceedance"
    PRECURSOR_ACTIVE = "precursor-active"
    PRE_EVENT_SIGNAL = "pre-event-signal"
    PROJECTED_IMPACT = "projected-impact"
    SOCIAL_COORDINATION = "social-coordination"
    SOCIAL_VELOCITY = "social-velocity"
    UNEXPLAINED_MOVE = "unexplained-move"

    def __str__(self) -> str:
        return str(self.value)

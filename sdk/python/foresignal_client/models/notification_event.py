from enum import StrEnum


class NotificationEvent(StrEnum):
    ALERT = "alert"
    ANALYST_NOTE = "analyst.note"
    ASSESSMENT_PUBLISHED = "assessment.published"
    BRIEFING = "briefing"
    FINDING_SIGNIFICANT = "finding.significant"
    FORECAST_RESOLUTION_NEEDED = "forecast.resolution_needed"
    HYPOTHESIS_BAND_CHANGED = "hypothesis.band_changed"
    SITUATION_ACTIVATED = "situation.activated"
    SITUATION_ESCALATED = "situation.escalated"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)

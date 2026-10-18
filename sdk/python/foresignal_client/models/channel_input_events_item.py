from enum import StrEnum


class ChannelInputEventsItem(StrEnum):
    ALERT = "alert"
    ANALYST_NOTE = "analyst.note"
    ASSESSMENT_PUBLISHED = "assessment.published"
    ASSUMPTION_BROKEN = "assumption.broken"
    BRIEFING = "briefing"
    CHRONICLE_ACCESS_REQUESTED = "chronicle.access.requested"
    CHRONICLE_CAP_WARNING = "chronicle.cap_warning"
    CHRONICLE_GAP = "chronicle.gap"
    CHRONICLE_PROPOSED = "chronicle.proposed"
    FINDING_SIGNIFICANT = "finding.significant"
    FORECAST_RESOLUTION_NEEDED = "forecast.resolution_needed"
    FORECAST_RESOLVED = "forecast.resolved"
    HYPOTHESIS_BAND_CHANGED = "hypothesis.band_changed"
    INDICATOR_OBSERVED = "indicator.observed"
    MENTION = "mention"
    PACKAGE_RECEIVED = "package.received"
    REPORT_RECEIVED = "report.received"
    SAVED_SEARCH_MATCHES = "saved-search.matches"
    SHARE = "share"
    SITUATION_ACTIVATED = "situation.activated"
    SITUATION_ESCALATED = "situation.escalated"
    SOCIAL_ATTENTION_GAP = "social.attention_gap"
    SOCIAL_DIVERGENCE = "social.divergence"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)

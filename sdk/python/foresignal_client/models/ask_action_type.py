from enum import StrEnum


class AskActionType(StrEnum):
    ACKNOWLEDGE_ALERT = "acknowledge_alert"
    ALERT_STATUS = "alert_status"
    APPLY_TUNING_SUGGESTION = "apply_tuning_suggestion"
    CREATE_BRIEFING = "create_briefing"
    CREATE_VIEW = "create_view"
    CREATE_WATCH = "create_watch"
    SET_ALERT_FEEDBACK = "set_alert_feedback"
    SNOOZE_ALERT = "snooze_alert"
    UPDATE_WATCH = "update_watch"

    def __str__(self) -> str:
        return str(self.value)

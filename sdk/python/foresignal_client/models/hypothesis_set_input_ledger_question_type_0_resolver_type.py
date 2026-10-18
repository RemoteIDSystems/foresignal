from enum import StrEnum


class HypothesisSetInputLedgerQuestionType0ResolverType(StrEnum):
    ALERT_FIRED = "alert-fired"
    COUNT_THRESHOLD = "count-threshold"
    EVENT_EXISTS = "event-exists"
    HYPOTHESIS_OUTCOME = "hypothesis-outcome"
    MANUAL = "manual"
    SERIES_VALUE = "series-value"
    SITUATION_STATUS = "situation-status"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class InvestigationItemItemType(StrEnum):
    ALERT = "alert"
    ANOMALY = "anomaly"
    ATTACHMENT = "attachment"
    BRIEFING_RUN = "briefing_run"
    EVENT = "event"
    EVIDENCE_ITEM = "evidence-item"
    NOTE = "note"
    OBJECT = "object"
    PLACE = "place"
    RECORD = "record"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class SituationMemberMemberType(StrEnum):
    ALERT = "alert"
    ANOMALY = "anomaly"
    FINDING = "finding"
    INTEL_EVENT = "intel_event"
    INVESTIGATION = "investigation"
    RECORD = "record"

    def __str__(self) -> str:
        return str(self.value)

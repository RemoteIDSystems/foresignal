from enum import StrEnum


class TriggerRuleInputType(StrEnum):
    ABSENCE = "absence"
    ANOMALY = "anomaly"
    COUNT = "count"
    RECORD = "record"

    def __str__(self) -> str:
        return str(self.value)

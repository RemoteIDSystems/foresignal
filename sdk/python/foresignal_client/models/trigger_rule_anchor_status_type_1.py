from enum import StrEnum


class TriggerRuleAnchorStatusType1(StrEnum):
    ACTIVE = "active"
    STALE = "stale"

    def __str__(self) -> str:
        return str(self.value)

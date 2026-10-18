from enum import StrEnum


class TriggerRuleInputCooldownScope(StrEnum):
    RECORD = "record"
    RULE = "rule"

    def __str__(self) -> str:
        return str(self.value)

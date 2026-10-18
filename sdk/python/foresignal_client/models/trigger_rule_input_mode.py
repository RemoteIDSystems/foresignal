from enum import StrEnum


class TriggerRuleInputMode(StrEnum):
    ENTER = "enter"
    EXIT = "exit"
    MATCH = "match"

    def __str__(self) -> str:
        return str(self.value)

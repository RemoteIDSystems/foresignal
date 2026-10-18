from enum import StrEnum


class RuleBacktestResultSamplesItemEdge(StrEnum):
    ENTER = "enter"
    EXIT = "exit"
    MATCH = "match"
    WINDOW = "window"

    def __str__(self) -> str:
        return str(self.value)

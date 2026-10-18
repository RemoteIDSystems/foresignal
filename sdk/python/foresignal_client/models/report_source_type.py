from enum import StrEnum


class ReportSourceType(StrEnum):
    CHRONICLE = "chronicle"
    INVESTIGATION = "investigation"
    SELECTION = "selection"
    SITUATION = "situation"
    TAG = "tag"
    WORKSPACE = "workspace"

    def __str__(self) -> str:
        return str(self.value)

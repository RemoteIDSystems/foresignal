from enum import StrEnum


class AccessGrantInputDeliverAs(StrEnum):
    CHRONICLE_EXTRACT = "chronicle-extract"
    REPORT = "report"

    def __str__(self) -> str:
        return str(self.value)

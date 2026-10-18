from enum import StrEnum


class AnalystFeedbackTarget(StrEnum):
    NOTE = "note"
    PROPOSAL = "proposal"

    def __str__(self) -> str:
        return str(self.value)

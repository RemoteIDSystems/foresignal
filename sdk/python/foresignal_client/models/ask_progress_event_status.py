from enum import StrEnum


class AskProgressEventStatus(StrEnum):
    CALLING = "calling"
    DONE = "done"

    def __str__(self) -> str:
        return str(self.value)

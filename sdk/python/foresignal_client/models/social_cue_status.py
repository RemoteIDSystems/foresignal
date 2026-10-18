from enum import StrEnum


class SocialCueStatus(StrEnum):
    BUDGET_REFUSED = "budget-refused"
    DONE = "done"
    FAILED = "failed"
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class ListSocialCuesStatusItem(StrEnum):
    BUDGET_REFUSED = "budget-refused"
    DONE = "done"
    FAILED = "failed"
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)

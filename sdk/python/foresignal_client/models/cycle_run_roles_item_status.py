from enum import StrEnum


class CycleRunRolesItemStatus(StrEnum):
    FAILED = "failed"
    NOT_CONFIGURED = "not-configured"
    OK = "ok"
    SKIPPED = "skipped"

    def __str__(self) -> str:
        return str(self.value)

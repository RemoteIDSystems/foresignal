from enum import StrEnum


class SystemInfoStatus(StrEnum):
    DEGRADED = "degraded"
    DOWN = "down"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)

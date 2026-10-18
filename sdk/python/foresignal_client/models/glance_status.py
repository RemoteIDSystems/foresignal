from enum import StrEnum


class GlanceStatus(StrEnum):
    DEGRADED = "degraded"
    FAILING = "failing"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)

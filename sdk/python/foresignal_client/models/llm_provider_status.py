from enum import StrEnum


class LlmProviderStatus(StrEnum):
    DEGRADED = "degraded"
    DISABLED = "disabled"
    FAILING = "failing"
    HEALTHY = "healthy"
    IDLE = "idle"

    def __str__(self) -> str:
        return str(self.value)

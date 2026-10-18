from enum import StrEnum


class SocialPlatformState(StrEnum):
    BUDGET_EXHAUSTED = "budget-exhausted"
    CREDENTIALS_REQUIRED = "credentials-required"
    DEGRADED = "degraded"
    DISABLED = "disabled"
    ENABLED = "enabled"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class PushSubscriptionStatus(StrEnum):
    ACTIVE = "active"
    DISABLED = "disabled"
    PENDING = "pending"

    def __str__(self) -> str:
        return str(self.value)

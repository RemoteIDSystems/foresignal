from enum import StrEnum


class VerifyWebSubSubscriptionHubMode(StrEnum):
    DENIED = "denied"
    SUBSCRIBE = "subscribe"
    UNSUBSCRIBE = "unsubscribe"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class PushSubscriptionTransportLabel(StrEnum):
    BROWSER = "browser"
    OTHER = "other"
    UNIFIEDPUSH = "unifiedpush"

    def __str__(self) -> str:
        return str(self.value)

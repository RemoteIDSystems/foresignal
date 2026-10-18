from enum import StrEnum


class PushSubscriptionInputPayloadFormat(StrEnum):
    DECLARATIVE = "declarative"
    FORESIGNAL = "foresignal"

    def __str__(self) -> str:
        return str(self.value)

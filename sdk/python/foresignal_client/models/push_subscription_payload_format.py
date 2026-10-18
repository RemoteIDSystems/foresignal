from enum import StrEnum


class PushSubscriptionPayloadFormat(StrEnum):
    DECLARATIVE = "declarative"
    FORESIGNAL = "foresignal"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class DeliveryTargetFormat(StrEnum):
    ALERT = "alert"
    AUDIO = "audio"
    EMAIL = "email"
    HTML = "html"
    PUSH = "push"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class PushProviderInputType(StrEnum):
    APNS = "apns"
    FCM = "fcm"

    def __str__(self) -> str:
        return str(self.value)

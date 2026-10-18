from enum import StrEnum


class NotificationActionMethod(StrEnum):
    POST = "POST"

    def __str__(self) -> str:
        return str(self.value)

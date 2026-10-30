from enum import StrEnum


class BriefingRunDeliveryResultsType0ItemFormat(StrEnum):
    ALERT = "alert"
    AUDIO = "audio"
    EMAIL = "email"
    HTML = "html"
    PUSH = "push"

    def __str__(self) -> str:
        return str(self.value)

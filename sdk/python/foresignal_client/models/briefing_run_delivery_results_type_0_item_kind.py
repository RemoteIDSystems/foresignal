from enum import StrEnum


class BriefingRunDeliveryResultsType0ItemKind(StrEnum):
    CHANNEL = "channel"
    PODCAST = "podcast"
    WEBSITE = "website"

    def __str__(self) -> str:
        return str(self.value)

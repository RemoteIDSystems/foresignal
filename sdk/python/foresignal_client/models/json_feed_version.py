from enum import StrEnum


class JsonFeedVersion(StrEnum):
    HTTPSJSONFEED_ORGVERSION1_1 = "https://jsonfeed.org/version/1.1"

    def __str__(self) -> str:
        return str(self.value)

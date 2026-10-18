from enum import StrEnum


class SourceAccessRoute(StrEnum):
    BROWSER = "browser"
    OFFICIAL = "official"
    OTHER = "other"
    SCRAPE = "scrape"
    UNOFFICIAL_API = "unofficial-api"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class GetReceivedPackageRenditionFormat(StrEnum):
    HTML = "html"
    JSON = "json"
    MD = "md"

    def __str__(self) -> str:
        return str(self.value)

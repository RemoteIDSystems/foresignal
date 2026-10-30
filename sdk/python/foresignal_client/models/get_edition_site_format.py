from enum import StrEnum


class GetEditionSiteFormat(StrEnum):
    HTML = "html"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)

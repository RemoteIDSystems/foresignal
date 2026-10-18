from enum import StrEnum


class GetLatestBriefingRunFormat(StrEnum):
    HTML = "html"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)

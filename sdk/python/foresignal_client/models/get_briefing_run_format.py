from enum import StrEnum


class GetBriefingRunFormat(StrEnum):
    HTML = "html"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)

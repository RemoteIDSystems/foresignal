from enum import StrEnum


class GetGlanceFormat(StrEnum):
    JSON = "json"
    TEXT = "text"

    def __str__(self) -> str:
        return str(self.value)

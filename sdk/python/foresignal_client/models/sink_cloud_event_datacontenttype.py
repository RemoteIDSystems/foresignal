from enum import StrEnum


class SinkCloudEventDatacontenttype(StrEnum):
    APPLICATIONJSON = "application/json"

    def __str__(self) -> str:
        return str(self.value)

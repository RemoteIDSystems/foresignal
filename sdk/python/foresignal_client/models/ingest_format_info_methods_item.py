from enum import StrEnum


class IngestFormatInfoMethodsItem(StrEnum):
    GET = "GET"
    POST = "POST"
    PUT = "PUT"

    def __str__(self) -> str:
        return str(self.value)

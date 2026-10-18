from enum import StrEnum


class CapturedSampleMethod(StrEnum):
    GET = "GET"
    POST = "POST"
    PUT = "PUT"

    def __str__(self) -> str:
        return str(self.value)

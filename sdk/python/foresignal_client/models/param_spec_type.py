from enum import StrEnum


class ParamSpecType(StrEnum):
    BBOX = "bbox"
    BOOLEAN = "boolean"
    DURATION = "duration"
    INBOX_PATH = "inbox-path"
    INTEGER = "integer"
    JSON = "json"
    LAN_URL = "lan-url"
    NUMBER = "number"
    STRING = "string"
    STRING_LIST = "string-list"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class ChannelTypeConfigFieldsItemType(StrEnum):
    BOOLEAN = "boolean"
    ENUM = "enum"
    INTEGER = "integer"
    MAP = "map"
    NUMBER = "number"
    STRING = "string"
    STRING_LIST = "string-list"
    URL = "url"

    def __str__(self) -> str:
        return str(self.value)

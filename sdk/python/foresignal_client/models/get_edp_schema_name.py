from enum import StrEnum


class GetEdpSchemaName(StrEnum):
    BATCH = "batch"
    DELETE = "delete"
    DOCUMENT = "document"
    EVENT = "event"
    FEATURE = "feature"
    FORECAST = "forecast"
    ITEM = "item"
    MANIFEST = "manifest"
    OBSERVATION = "observation"
    STATUS = "status"

    def __str__(self) -> str:
        return str(self.value)

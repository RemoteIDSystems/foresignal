from enum import StrEnum


class CollectionGapRecommendationsItemType(StrEnum):
    ADD_RECEIVER = "add-receiver"
    ADD_SENSOR = "add-sensor"
    ENABLE_SOURCE = "enable-source"
    NONE = "none"

    def __str__(self) -> str:
        return str(self.value)

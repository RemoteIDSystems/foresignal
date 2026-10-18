from enum import StrEnum


class TileKeyCreatePrefixesItem(StrEnum):
    VALUE_0 = "/v1/products/"
    VALUE_1 = "/v1/styles"
    VALUE_2 = "/v1/media/"
    VALUE_3 = "/v1/tiles/"
    VALUE_4 = "/v1/maps/static"
    VALUE_5 = "/v1/sample"
    VALUE_6 = "/v1/ground-stations/"
    VALUE_7 = "/v1/changes/stream"
    VALUE_8 = "/v1/events/stream"
    VALUE_9 = "/v1/map/"

    def __str__(self) -> str:
        return str(self.value)

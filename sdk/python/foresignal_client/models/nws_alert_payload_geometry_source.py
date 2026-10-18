from enum import StrEnum


class NwsAlertPayloadGeometrySource(StrEnum):
    COUNTY_UNION = "county-union"
    NONE = "none"
    POLYGON = "polygon"

    def __str__(self) -> str:
        return str(self.value)

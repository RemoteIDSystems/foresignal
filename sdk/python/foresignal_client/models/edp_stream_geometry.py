from enum import StrEnum


class EdpStreamGeometry(StrEnum):
    ANY = "any"
    LINESTRING = "LineString"
    MULTIPOLYGON = "MultiPolygon"
    NONE = "none"
    POINT = "Point"
    POLYGON = "Polygon"

    def __str__(self) -> str:
        return str(self.value)

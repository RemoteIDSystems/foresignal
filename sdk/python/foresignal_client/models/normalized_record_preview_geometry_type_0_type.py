from enum import StrEnum


class NormalizedRecordPreviewGeometryType0Type(StrEnum):
    GEOMETRYCOLLECTION = "GeometryCollection"
    LINESTRING = "LineString"
    MULTILINESTRING = "MultiLineString"
    MULTIPOINT = "MultiPoint"
    MULTIPOLYGON = "MultiPolygon"
    POINT = "Point"
    POLYGON = "Polygon"

    def __str__(self) -> str:
        return str(self.value)

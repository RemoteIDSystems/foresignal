from enum import StrEnum


class OgcItemsBboxCrs(StrEnum):
    HTTPWWW_OPENGIS_NETDEFCRSOGC1_3CRS84 = (
        "http://www.opengis.net/def/crs/OGC/1.3/CRS84"
    )

    def __str__(self) -> str:
        return str(self.value)

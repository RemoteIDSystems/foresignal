from enum import StrEnum


class ExternalCatalogueEntryProtocol(StrEnum):
    WMS = "wms"
    WMTS = "wmts"
    XYZ = "xyz"

    def __str__(self) -> str:
        return str(self.value)

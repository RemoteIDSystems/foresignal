from enum import StrEnum


class DossierSectionsImageryItemsItemKind(StrEnum):
    RASTER_FRAME = "raster-frame"
    SATDUMP_PRODUCT = "satdump-product"
    STAC_SCENE = "stac-scene"

    def __str__(self) -> str:
        return str(self.value)

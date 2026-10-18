from enum import StrEnum


class SearchTransitCatalogEntityType(StrEnum):
    SA = "sa"
    TU = "tu"
    VP = "vp"

    def __str__(self) -> str:
        return str(self.value)

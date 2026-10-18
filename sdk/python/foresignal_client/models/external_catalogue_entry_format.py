from enum import StrEnum


class ExternalCatalogueEntryFormat(StrEnum):
    JPG = "jpg"
    PNG = "png"

    def __str__(self) -> str:
        return str(self.value)

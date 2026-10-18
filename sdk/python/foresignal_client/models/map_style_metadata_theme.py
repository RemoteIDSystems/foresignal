from enum import StrEnum


class MapStyleMetadataTheme(StrEnum):
    DARK = "dark"
    LIGHT = "light"

    def __str__(self) -> str:
        return str(self.value)

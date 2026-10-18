from enum import StrEnum


class SourceSettingsRegionTagging(StrEnum):
    EAGER = "eager"
    LAZY = "lazy"
    OFF = "off"

    def __str__(self) -> str:
        return str(self.value)

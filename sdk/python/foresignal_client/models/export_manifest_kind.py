from enum import StrEnum


class ExportManifestKind(StrEnum):
    EXPORT = "EXPORT"
    PACK = "PACK"
    SNAPSHOT = "SNAPSHOT"

    def __str__(self) -> str:
        return str(self.value)

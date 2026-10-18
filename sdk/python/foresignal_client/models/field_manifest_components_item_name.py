from enum import StrEnum


class FieldManifestComponentsItemName(StrEnum):
    U = "u"
    V = "v"

    def __str__(self) -> str:
        return str(self.value)

from enum import StrEnum


class GetProductFieldManifestRes(StrEnum):
    VALUE_0 = "1"
    VALUE_1 = "0.25"

    def __str__(self) -> str:
        return str(self.value)

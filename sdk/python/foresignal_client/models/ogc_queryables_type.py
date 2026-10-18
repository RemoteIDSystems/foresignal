from enum import StrEnum


class OgcQueryablesType(StrEnum):
    OBJECT = "object"

    def __str__(self) -> str:
        return str(self.value)

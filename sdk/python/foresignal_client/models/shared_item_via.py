from enum import StrEnum


class SharedItemVia(StrEnum):
    GROUP = "group"
    INSTANCE = "instance"
    PRINCIPAL = "principal"

    def __str__(self) -> str:
        return str(self.value)

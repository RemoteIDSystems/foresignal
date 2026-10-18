from enum import StrEnum


class GetProductTileForesignalPrefetch(StrEnum):
    VALUE_0 = "1"

    def __str__(self) -> str:
        return str(self.value)

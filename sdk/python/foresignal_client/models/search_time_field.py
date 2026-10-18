from enum import StrEnum


class SearchTimeField(StrEnum):
    EFFECTIVE = "effective"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)

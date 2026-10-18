from enum import StrEnum


class GetStatsTopBy(StrEnum):
    CELL = "cell"
    ENTITY = "entity"
    KIND = "kind"
    REGION = "region"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)
